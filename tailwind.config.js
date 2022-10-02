/** @type {import('tailwindcss').Config} */
module.exports = {
  purge: ["./apps/uts-dpm-frontend/src/**/*.html", "./apps/uts-dpm-frontend/src/**/*.ts"],
  content: [
    "./apps/uts-dpm-frontend/src/**/*.{html,ts}",
  ],
  theme: {
    extend: {},
  },
  plugins: [require("daisyui")],
  daisyui: {
    themes: ["bumblebee", "cupcake", "dark", "cmyk"],
  },
}
