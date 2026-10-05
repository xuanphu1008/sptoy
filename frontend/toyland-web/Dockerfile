# ---- Buoc 1: build Angular (production) ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration production

# ---- Buoc 2: phuc vu file tinh bang Nginx ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/toyland-web/browser /usr/share/nginx/html
EXPOSE 80
