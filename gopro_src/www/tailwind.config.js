/** @type {import('tailwindcss').Config} */
module.exports = {
  darkMode: 'class',
  content: [
    "./*.{html,js}", // Scanne les fichiers .html et .js à la racine
    "./copie/*.{html,js}", // Inclut aussi les fichiers dans le dossier copie s'ils sont pertinents
    // Ajoutez d'autres chemins si nécessaire, par exemple : "./src/**/*.{html,js}"
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
