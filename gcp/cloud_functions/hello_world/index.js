exports.helloWorld = function helloWorld (event, callback) {
  console.log("My Cloud Function");
  console.log(event);
  callback();
};
