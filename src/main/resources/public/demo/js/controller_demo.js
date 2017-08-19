'use strict';

Fox.DemoCtrl = function($routeParams, $scope, $http) {

// demo data
$scope.democtrl = {};
$scope.currentCtrl = 'DemoCtrl';

$scope.request = {
    type: 'text',
    task: 'ner',
    input: '',
    output: 'Turtle',
    nif: 0,
    foxlight: 'OFF',
    defaults: 0,
    state: 'done'
};

$scope.response = {
    input: '',
    output: '',
    log: ''
};

// register directive function
$scope.setDirectiveFn = function(directiveFn){
  $scope.directiveFn = directiveFn;
}

// send data for annotations
var docData = {text: "",entities:[]};
$scope.embed = function(){
  $scope.directiveFn();
  Util.embed('annotations', collData, docData,webFontURLs);
}
// get cfg
$http({method: 'GET', url: 'http://' + $scope.host + '/config'}).
success(function(data, status, headers, config) {
    $scope.config = data;
    Util.embed('annotations', collData, docData,webFontURLs);
}).
error(function(data, status, headers, config) {
  console.log(status);
});

//
// reads turtle to store
//
var queryPhrase = 'PREFIX rdf:  <' + ns_rdf + '> ' +
                  'PREFIX nif:  <' + ns_nif + '> ' +
                    'SELECT ?s WHERE { ?s a nif:Phrase . } ';

var queryContext = 'PREFIX rdf:  <' + ns_rdf + '> ' +
                  'PREFIX nif:  <' + ns_nif + '> ' +
                  'SELECT ?s WHERE { ?s a nif:Context . } ';

var loadStore = function(turtle,callback){
  rdfstore.create(function(err, store) {
     store.setPrefix("nif", '<' + ns_nif + '> ');
     store.setPrefix("its", '<' + ns_its + '> ');
     store.load("text/turtle", turtle, function(err, results) {
       callback(store);
     });
   });
};

var queryStore = function(store, query, callback){
  // queery phrases
  store.execute(query, function(err, results) {
      if (!err) {
          for (var i = 0; i < results.length; i++) {
              store.node(results[i].s.value, function(err, graph){
                if (!err) {
                  callback(graph.triples)
                }
              });
          }
      }
  });//execute
}

// triples for a specific subject
var showPhrases =  function(triples,callback) {
  var
      uri = triples[0].subject.nominalValue,
      type,beginIndex,endIndex;

  for (var i = 0; i < triples.length; i++) {
    uri = triples[i].subject.nominalValue;

      if (triples[i].predicate.nominalValue == nif.beginIndex ){
          beginIndex = triples[i].object.nominalValue;
      }else
      if (triples[i].predicate.nominalValue == nif.endIndex ){
          endIndex = triples[i].object.nominalValue;
      } else
      if (triples[i].predicate.nominalValue == its.taClassRef &&
      triples[i].object.nominalValue.startsWith(ns_foxo) ){
          type = triples[i].object.nominalValue.substring(ns_foxo.length);
      }
  }
  docData.entities.push([uri, type, [[beginIndex, endIndex]]]);
  callback();
};

//triples for a specific subject
var showContext =  function(triples,callback) {
  for (var i = 0; i < triples.length; i++) {
      if (triples[i].predicate.nominalValue == nif.isString ){
        docData.text = triples[i].object.nominalValue;
      }
  }
  callback();
};

var parseTutle = function(turtle,callback) {
  docData = {
      text     : "",
      entities : [// Format: [${ID}, ${TYPE}, [[${START},${END}]])
      ],
      relations :[ // Format: [${ID}, ${TYPE}, [[${ARGNAME}, ${TARGET}], [${ARGNAME}, ${TARGET}]]]
        //  ['R1', 'Anaphora', [['Anaphor', 'T2'], ['Entity', 'T1']]]
      ]
  };

  loadStore(turtle,function(store){
    queryStore(store, queryContext, function(triples){
        showContext(triples,function(){
          queryStore(store, queryPhrase, function(triples){
              showPhrases(triples,callback);
          });
        });
    });
  });
};


// send demo request
$scope.request.send = function() {
    $scope.request.state = 'sending';

    // clear old data
    $scope.response = {input: '',output: '', log: ''};


    // prepare request
    var request = angular.copy($scope.request);

    // send request
    $http({
        method: 'POST',
        url: 'http://' + $scope.host + '/fox',
        data: request
    }).
    success(function(data, status, headers, config) {

      // demo response data post process
      parseTutle(data,function(){
          $scope.embed();
      });
      $scope.response = {
          output: data
      };
      $scope.request.state = 'done';
    }).
    error(function(data, status, headers, config) {
        $scope.request.state = 'done';
    });
};

$scope.$watch('request.lang', function(newValue, oldValue) {
    $scope.request.foxlight = 'OFF';
});

// remove that
$scope.$watch('request.defaults', function(newValue, oldValue) {

    $scope.request.lang = 'en';

    if (newValue == 1) {
        $scope.request.lang = 'de';
        $scope.request.type = 'text';
        $scope.request.input = "Diese Aufnahme mit Pablo Escobar seiner Frau Victoria Eugenia Henau und Sohn Juan Pablo suggeriert ein ganz normales Familienleben.";
        $scope.request.output = 'Turtle';

    } else if (newValue == 2) {
        $scope.request.lang = 'fr';
        $scope.request.type = 'text';
        $scope.request.input = "Gottfried Wilhelm Leibniz né à Leipzig, le 1er juillet 1646 est un philosophe, scientifique, mathématicien, logicien, diplomate, juriste, bibliothécaire et philologue allemand qui a écrit en latin, allemand et français. Leibniz mort à Hanovre, le 14 novembre 1716.";
        $scope.request.output = 'Turtle';
    } else if (newValue == 3) {
        $scope.request.input = "In 1672 vertrok Leibniz naar Parijs. Toen was zijn wiskundige kennis beperkt tot die van het Oude Griekenland. Christiaan Huygens begeleidde Leibniz in zijn studies en vertelde hem welke eigentijdse problemen hij moest bestuderen.";
        $scope.request.lang = 'nl';
        $scope.request.type = 'text';

        $scope.request.output = 'Turtle';

    } else if (newValue == 4) {
        $scope.request.lang = 'en';

        $scope.request.type = 'text';
        $scope.request.input = "Cologne German: Köln, Kölsch: Kölle is Germany's fourth-largest city (after Berlin, Hamburg, and Munich), and is the largest city both in the German Federal State of North Rhine-Westphalia and within the Rhine-Ruhr Metropolitan Area, one of the major European metropolitan areas with more than ten million inhabitants. Cologne is located on both sides of the Rhine River. The city's famous Cologne Cathedral (Kölner Dom) is the seat of the Catholic Archbishop of Cologne. The University of Cologne (Universität zu Köln) is one of Europe's oldest and largest universities.";
        $scope.request.output = 'Turtle';

    } else if (newValue == 5) {
        $scope.request.lang = 'es';
        $scope.request.type = 'text';
        $scope.request.input = "Gottfried Leibniz nació el 1 de julio de 1646 en Leipzig, dos años antes de que terminara la Guerra de los Treinta Años, hijo de Federico Leibniz, jurista y profesor de filosofía moral en la Universidad de Leipzig, y Catherina Schmuck, hija de un profesor de leyes.";
        $scope.request.output = 'Turtle';

    } else { // default 0 or not def.
        $scope.request.lang = 'en';
        $scope.request.type = 'text';
        $scope.request.input = "The philosopher and mathematician Leibniz was born in Leipzig in 1646 and attended the University of Leipzig from 1661-1666. The current chancellor of Germany, Angela Merkel, also attended this university. ";
        $scope.request.output = 'Turtle';
    }
});
};

Fox.controller('DemoCtrl', Fox.DemoCtrl);
