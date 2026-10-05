// Dia chi REST API cua backend Spring Boot.
// - Chay tren may (npm start / Docker / may khac trong mang LAN): cung ten may, cong 8080.
// - Ban deploy tren Render: goi sang dich vu backend tren Render.
const host = location.hostname;

export const environment = {
  apiUrl: host.endsWith('.onrender.com')
    ? 'https://sptoy-api.onrender.com/api/v1'
    : `http://${host}:8080/api/v1`,
};
