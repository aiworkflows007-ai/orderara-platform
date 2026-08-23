FROM node:20-alpine AS builder
WORKDIR /app

# Build Admin & Subscription Portal
COPY admin-panel/package*.json ./admin-panel/
RUN cd admin-panel && npm install
COPY admin-panel/ ./admin-panel/
RUN cd admin-panel && npm run build

# Prepare Backend
COPY backend/package*.json ./backend/
RUN cd backend && npm install --omit=dev
COPY backend/ ./backend/

FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV PORT=3000

COPY --from=builder /app/backend ./backend
COPY --from=builder /app/admin-panel/dist ./admin-panel/dist

EXPOSE 3000
CMD ["node", "backend/server.js"]
