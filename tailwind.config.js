const daisyui = require("daisyui");

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./build/*.html", "./build/js/**/*.js"],
  plugins: [daisyui],
  daisyui: {
    themes: ["light", "dark"],
  },
};
