require("dotenv").config();

const express = require("express");
const cors = require("cors");

const { apiLimiter } = require("./security/rateLimiter");

const app = express();

/* ============================
   MIDDLEWARE
============================ */

app.use(cors());
app.use(express.json()); // required for req.body

/* ============================
   REQUEST LOGGER
============================ */

app.use((req, res, next) => {
  console.log("Incoming request:", req.method, req.url);
  next();
});

/* ============================
   HEALTH CHECK
============================ */

app.get("/health", (req, res) => {
  res.json({
    status: "ConsentLens backend running"
  });
});

/* ============================
   ROUTES
============================ */

const routes = require("./api-gateway/routes");

app.use("/api", apiLimiter, routes);

/* ============================
   START SERVER
============================ */

const PORT = process.env.PORT || 5000;

app.listen(PORT, "0.0.0.0", () => {
  console.log(`ConsentLens backend running on port ${PORT}`);
});