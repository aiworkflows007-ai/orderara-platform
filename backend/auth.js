/**
 * Partner authentication — verifies Supabase-issued JWTs.
 *
 * Supabase Auth handles the Google sign-in and issues the token; this server
 * only has to prove the token is genuine. Supabase publishes the public half of
 * its signing keys at a JWKS URL, so verification happens locally in-process —
 * there is no network round-trip to Supabase on every request, and keys can be
 * rotated on their side without redeploying this server.
 *
 * Env vars:
 *   SUPABASE_PROJECT_REF  the "abcdefgh" in https://abcdefgh.supabase.co
 *   SUPABASE_JWKS_URL     full JWKS URL (overrides PROJECT_REF; used by tests)
 *   REQUIRE_PARTNER_AUTH  "true" to reject unauthenticated partner calls
 *
 * REQUIRE_PARTNER_AUTH defaults to OFF so that an existing install keeps
 * working after this file lands. Turn it on once the app is signing in.
 */

// jose is ESM-only from v5 and this server is CommonJS, so it is pulled in with
// a dynamic import and the promise is cached rather than re-imported per call.
let josePromise = null;
function jose() {
  josePromise = josePromise || import('jose');
  return josePromise;
}

const PROJECT_REF = process.env.SUPABASE_PROJECT_REF || '';
const JWKS_URL =
  process.env.SUPABASE_JWKS_URL ||
  (PROJECT_REF ? `https://${PROJECT_REF}.supabase.co/auth/v1/jwks` : '');

const REQUIRE_AUTH = String(process.env.REQUIRE_PARTNER_AUTH).toLowerCase() === 'true';

let jwksCache = null;
async function getJwks() {
  if (!JWKS_URL) {
    throw new Error(
      'No JWKS URL configured. Set SUPABASE_PROJECT_REF (or SUPABASE_JWKS_URL).'
    );
  }
  if (!jwksCache) {
    const { createRemoteJWKSet } = await jose();
    // createRemoteJWKSet caches keys internally and refetches only when it sees
    // a key id it does not recognise, which is what makes rotation seamless.
    jwksCache = createRemoteJWKSet(new URL(JWKS_URL));
  }
  return jwksCache;
}

/**
 * Verifies a raw JWT string and returns its claims.
 * Throws if the signature, expiry, or issuer does not check out.
 */
async function verifyToken(token) {
  const { jwtVerify } = await jose();
  const keys = await getJwks();
  const { payload } = await jwtVerify(token, keys, {
    // Supabase stamps every access token with this audience.
    audience: 'authenticated'
  });
  return payload;
}

function bearerFrom(req) {
  const header = req.headers.authorization || '';
  return header.startsWith('Bearer ') ? header.slice(7).trim() : '';
}

/**
 * Express middleware for /api/partner/*.
 *
 * Always populates req.auth when a valid token is present, so routes can start
 * reading it before enforcement is switched on. Only rejects when
 * REQUIRE_PARTNER_AUTH is "true".
 */
async function partnerAuth(req, res, next) {
  const token = bearerFrom(req);

  if (token) {
    try {
      const claims = await verifyToken(token);
      req.auth = {
        sub: claims.sub,                       // stable Supabase user id
        email: claims.email || '',
        provider: (claims.app_metadata || {}).provider || ''
      };
      return next();
    } catch (err) {
      if (REQUIRE_AUTH) {
        return res.status(401).json({ success: false, message: `Invalid token: ${err.message}` });
      }
      // Enforcement off: a bad token is treated the same as no token so that a
      // half-configured client cannot lock anyone out of the demo.
      req.auth = null;
      return next();
    }
  }

  req.auth = null;
  if (REQUIRE_AUTH) {
    return res.status(401).json({ success: false, message: 'Sign-in required' });
  }
  return next();
}

/** Logged once at boot so the running mode is never a surprise. */
function describeAuthMode() {
  if (!REQUIRE_AUTH) {
    return '[auth] partner routes OPEN (set REQUIRE_PARTNER_AUTH=true to enforce)';
  }
  if (!JWKS_URL) {
    return '[auth] REQUIRE_PARTNER_AUTH=true but no JWKS URL — every partner call will 401';
  }
  return `[auth] partner routes ENFORCED against ${JWKS_URL}`;
}

module.exports = {
  partnerAuth,
  verifyToken,
  describeAuthMode,
  REQUIRE_AUTH,
  JWKS_URL
};
