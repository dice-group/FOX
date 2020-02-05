'use strict';

Fox.DemoCtrl = function($routeParams, $scope, $http) {

  String.prototype.hashCode = function() {
    var hash = 0, i, chr;
    if (this.length === 0) return hash;
    for (i = 0; i < this.length; i++) {
      chr   = this.charCodeAt(i);
      hash  = ((hash << 5) - hash) + chr;
      hash |= 0; // Convert to 32bit integer
    }
    return hash;
  };

// demo data
$scope.currentCtrl = 'DemoCtrl';
$scope.response = {};

// register directive function
$scope.setDirectiveFn = function(directiveFn){
  $scope.directiveFn = directiveFn;
};

$scope.embed = function(){
  $scope.directiveFn();
  Util.embed('annotations', collData, docData, webFontURLs);
};

// get cfg
$http({method: 'GET', url: '/config'}).
  success(function(data, status, headers, config) {
    $scope.config = data;
    Util.embed('annotations', collData, docData, webFontURLs);
  }).error(function(data, status, headers, config) {
    console.error(status);
});

/*
 Gets the subject URIs which have the given uri as rdf type.
*/
var queryForTypURI = function(o){
  return 'SELECT ?s WHERE {  \
            ?s <' + voc.rdf.type + '> <' + o + '> .\
          } ';
};

/*
*/
var queryForRelationExtraction = function(){
  return 'SELECT ?s ?u ?g WHERE { \
            ?s  <' + voc.rdf.type + '> <' + voc.foxo.RelationExtraction + '>; \
                <' + voc.prov.used + '> ?u ; \
                <' + voc.prov.generated + '> ?g . \
          } ';
};

/*
  @param  uri of a generated relation e.g. 'http://ns.aksw.org/fox/resource#1535891212055'.
*/
var queryForRelation = function(g){
  return 'SELECT ?b ?s ?p ?o ?d ?sp ?op WHERE {\
          <' + g + '> <'+ voc.oa.hasTarget +'> ?b . \
          <' + g + '> <'+ voc.rdf.subject +'> ?s . \
          <' + g + '> <'+ voc.rdf.predicate +'> ?p . \
          <' + g + '> <'+ voc.rdf.object +'> ?o . \
          <' + g + '> <'+ voc.foxo.subjectphrase +'> ?sp . \
          <' + g + '> <'+ voc.foxo.objectphrase +'> ?op . \
          ?b <'+ voc.oa.hasSource +'> ?d \
        }';
};

/*
*/
var queryForRef = function(referenceContext,taIdentRef ){
  return 'SELECT ?us  WHERE {\
           ?us  <'+ voc.nif.referenceContext +'> <'+referenceContext+'> . \
           ?us  <'+ voc.its.taIdentRef +'> <'+taIdentRef+'> . \
       }';
};

/*
  loads turtle to store and callbacks it
*/
var loadStore = function(turtle,callback){
  rdfstore.create(function(err, store) {
     store.load("text/turtle", turtle, function(err, results) {
       if (!err) {
         callback(store);
       }else {
         console.error(err);
       }
     });
   });
};

/*
 executes query to store and callbacks results
*/
var queryStore = function(store, query, callback){
  store.execute(query, function(err, results) {
      if (!err) {
        callback(results);
      }else {
        console.error(err);
      }
  });
};

/*
 pushs entities to the brat data
*/
var showPhrases =  function(store,triples,callback) {
  for (var i = 0; i < triples.length; i++) {
    store.node(triples[i].s.value, function(err, graph){
        if (!err) {
          var uri, type,beginIndex,endIndex;
          for (var i = 0; i < graph.triples.length; i++) {

            uri = graph.triples[i].subject.nominalValue;

            if (graph.triples[i].predicate.nominalValue == voc.nif.beginIndex ){
                beginIndex = graph.triples[i].object.nominalValue;
            }else
            if (graph.triples[i].predicate.nominalValue == voc.nif.endIndex ){
                endIndex = graph.triples[i].object.nominalValue;
            } else
            if (graph.triples[i].predicate.nominalValue == voc.its.taClassRef &&
            graph.triples[i].object.nominalValue.startsWith(voc.ns_foxo) ){
                type = graph.triples[i].object.nominalValue.substring(voc.ns_foxo.length);
            }
          }
          docData.entities.push([uri, type, [[beginIndex, endIndex]]]);
          if(docData.entities.length == triples.length){
            callback();
          }
        }
      });
  }
};

/*
triples for a specific subject
*/
var showContext =  function(store, results, callback) {
  if(results.length > 0){
    store.node(results[0].s.value, function(err, graph){
        if (!err) {
          for (var i = 0; i < graph.triples.length; i++) {
              if (graph.triples[i].predicate.nominalValue == voc.nif.isString ){
                docData.text = graph.triples[i].object.nominalValue;
                break;
              }
          }
        }
        callback();
      });
    }else {
      callback();
    }
};

/*
  @param
  @return array of relations
*/
var getRelations = function(store, data, callback){
  var annos = [];
  // for all generated of the tools
  for (var i = 0; i < data.length; i++) {
    // for the generated of one tool
    var used = data[i].u.value; // tool
    var g = data[i].g.value; // generated
    getGenerated(store, g,used,function(anno){
      annos.push(anno);
      if(annos.length == data.length)
        callback(null,annos);
    });
  } // end for
};


var getGenerated = function(store,g,used, callback){
  store.execute(queryForRelation(g), function(err, results) {
    if (!!err){ console.error(err); } else {
      var found = false;
      var anno = {};
      anno.used = used;
      anno.hasTarget = results[0].b.value;
      anno.subject = results[0].s.value;
      anno.predicate = results[0].p.value;
      anno.object = results[0].o.value;
      anno.hasSource = results[0].d.value;

      anno.subjectRef = results[0].sp.value;
      anno.objectRef = results[0].op.value;
      callback(anno);
      /*
      store.execute(queryForRef(anno.hasSource, anno.subject), function(err, innresults) {
        if (!!err){ console.error(err);callback(); } else
        if (!err && innresults.length > 0){
          anno.subjectRef = innresults[0].us.value;
          if(!!found)
            callback(anno);
          found =true;

        }else callback();
      });// end store.execute

      store.execute(queryForRef(anno.hasSource, anno.object), function(err, innresults) {
        if (!!err){ console.error(err);callback(); } else
        if (!err && innresults.length > 0){
          anno.objectRef = innresults[0].us.value;
          if(!!found)
            callback(anno);
         found = true;
        }else callback();
      });// end store.execute
      */
    } // end if
  }); // end store.execute
};

/*
  handles FOX response to show annoations in brat
*/
var handleResponse = function(turtle,callback) {
  // clean data
  collData.relation_types = [];
  docData = { text: "", entities: [], relations:[]};
  Util.embed('annotations', collData, docData, webFontURLs);

// load store with data
loadStore(turtle,function(store){

  var contextDone = false;
  var phrasesDone = false;
  var relationsDone = false;

  // handles Context annotation
  queryStore(store, queryForTypURI(voc.nif.Context), function(data){
    showContext(store, data,function(){
      contextDone=true;
      if(contextDone && phrasesDone && relationsDone) {
        callback();
      }
    });
  });

  // handles Phrase annotation
  queryStore(store, queryForTypURI(voc.nif.Phrase), function(data){
    showPhrases(store, data, function(){
      phrasesDone = true;
      if(contextDone && phrasesDone && relationsDone) {
        callback();
      }
    });
  });

  // handles Relation annotation
  queryStore(store, queryForRelationExtraction(), function(data){
    if(data.length > 0){
      getRelations(store, data, function(err, annos){
          if (!!err){ console.error(err); } else {

            var getEnd = function (str,sep) {
                return str.substring(str.lastIndexOf(sep)+1, str.length);
            };

            var colors = ['darkblue', 'darkred', 'darkolivegreen', 'darkgoldenrod'];

            var tools = [];
            function uniqSorted(a) {
                return a.sort().filter(function(item, pos, ary) {
                    return !pos || item != ary[pos - 1];
                });
            }
            for (var j = 0; j < annos.length; j++) {
              tools.push(getEnd(annos[j].used, '.'));
            }
            tools = uniqSorted(tools);

            for (var j = 0; j < annos.length; j++) {

                var p = getEnd(annos[j].used, '.') +':'+getEnd(getEnd(annos[j].predicate,'/'),'#');
              // adds types
              var color = colors[tools.lastIndexOf(getEnd(annos[j].used, '.'))];
              collData.relation_types.push({
                  type     :p,
                  labels   : [p,p],
                  dashArray: '3,3',
                  color    :  color
              });

              // adds instances
              var id = 'R'+j;

              var e1 = annos[j].subjectRef;
              var e2 = annos[j].objectRef;
              docData.relations.push([id,p, [['A', e1], ['B', e2]]]);
            }

            relationsDone=true;
            if(contextDone && phrasesDone && relationsDone) {
              callback();
            }
          }
      }); // end getRelations
    }else{
      // no relations
      relationsDone=true;
      if(contextDone && phrasesDone && relationsDone) {
        callback();
      }
    }
  }); // end queryStore
}); // end loadStore
};

$scope.sendTest = function() {
  console.log('Start test...');

  var file = (!Math.round(Math.random()))?
  'http://0.0.0.0:4444/test/test.ttl':'http://0.0.0.0:4444/test/test2.ttl';

  // get test file content
  $http({method: 'GET', url: file}).
      success(function(data, status, headers, config) {
        handleResponse(data,function(){
            $scope.embed();
            console.log('Done test...');
        });
      }); // end http
};

/*
 send demo request
*/
$scope.send = function() {
  console.log('Start...');
  $scope.request.state = 'sending';
  $scope.response = {};
  var request = angular.copy($scope.request);

  // send request
  $http({
      method: 'POST',
      url: '/fox',
      data: request
  }).
  success(function(data, status, headers, config) {
    handleResponse(data,function(){
        $scope.embed();
        console.log('Done.');
    });

    $scope.response = data;
    $scope.request.state = 'done';
  }).
  error(function(data, status, headers, config) {
    console.err(status);
    $scope.request.state = 'done';
  });
};

/*
*/
$scope.$watch('request.lang', function(newValue, oldValue) {
    $scope.request.foxlight = 'OFF';
});

/*
*/
$scope.$watch('request.defaults', function(newValue, oldValue) {
  if(newValue === undefined ) newValue= 0;
  $scope.request = examples[newValue];
  $scope.request.defaults = newValue;
});

}; // end Fox.DemoCtrl
Fox.controller('DemoCtrl', Fox.DemoCtrl);
