# --- Dependencies stage ---
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --omit=dev

# --- Runtime stage ---
FROM node:20-alpine
WORKDIR /app

# Run as a non-root user for defense in depth.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=deps /app/node_modules ./node_modules
COPY . .

RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

ENV NODE_ENV=production
# Default port; actual value still comes from process.env.PORT at runtime.
EXPOSE 5000

CMD ["node", "src/server.js"]
