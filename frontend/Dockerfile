# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL=""
ARG VITE_SHOW_DEV_TOOLS="false"
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_SHOW_DEV_TOOLS=$VITE_SHOW_DEV_TOOLS
RUN npm run build

# Stage 2: Serve
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx-prod.conf /etc/nginx/nginx-prod.conf
# When VITE_API_URL is set (prod/Railway), use the no-proxy config
ARG VITE_API_URL=""
RUN if [ -n "$VITE_API_URL" ]; then cp /etc/nginx/nginx-prod.conf /etc/nginx/conf.d/default.conf; fi
EXPOSE 80
