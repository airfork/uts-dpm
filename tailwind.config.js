/** @type {import('tailwindcss').Config} */
module.exports = {
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
