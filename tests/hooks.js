export const tests = {}

global.it = function (name, test) {
  tests[name] = test
}
