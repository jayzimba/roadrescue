module.exports = {
  root: true,
  env: {
    es2022: true,
    node: true,
  },
  extends: ["eslint:recommended", "google"],
  rules: {
    "max-len": ["error", {code: 100}],
    "require-jsdoc": "off",
  },
};
