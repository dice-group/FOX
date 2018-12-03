
//var serverAddress = 'http://localhost:4444';
//var serverAddress = '';

/**
// Load Brat libraries
var bratLocation = 'lib/brat';
head.js(
  // External libraries
  bratLocation + '/client/lib/jquery.min.js',
  bratLocation + '/client/lib/jquery.svg.min.js',
  bratLocation + '/client/lib/jquery.svgdom.min.js',

  // brat helper modules
  bratLocation + '/client/src/configuration.js',
  bratLocation + '/client/src/util.js',
  bratLocation + '/client/src/annotation_log.js',
  bratLocation + '/client/lib/webfont.js',

  // brat modules
  bratLocation + '/client/src/dispatcher.js',
  bratLocation + '/client/src/url_monitor.js',
  bratLocation + '/client/src/visualizer.js'
);
*/

var loc = 'lib/brat';
var webFontURLs = [
    loc + '/static/fonts/Astloch-Bold.ttf',
    loc + '/static/fonts/PT_Sans-Caption-Web-Regular.ttf',
    loc + '/static/fonts/Liberation_Sans-Regular.ttf'
];

var collData = {
    entity_types: [ {
            type   : 'PERSON', labels : ['Person', 'Person'],
            bgColor: '#7fa2ff', borderColor: 'darken'
    },{
            type   : 'LOCATION', labels : ['Place', 'Place'],
            bgColor: '#468746', borderColor: 'darken'
    }, {
            type   : 'ORGANIZATION', labels : ['Organisation', 'Organisation'],
            bgColor: '#AF4A52', borderColor: 'darken'
    }
   ],
    relation_types: [{
        type     : 'Anaphora',
        labels   : ['Anaphora', 'Ana'],
        dashArray: '3,3',
        color    : 'purple',
        args     : [
            {role: 'A', targets: ['Person'] },
            {role: 'B',  targets: ['Person'] }
        ]
    }]
};

// clean data
var docData = {
    text     : "",
    entities : [// Format: [${ID}, ${TYPE}, [[${START},${END}]])
    ],
    relations :[ // Format: [${ID}, ${TYPE}, [[${ARGNAME}, ${TARGET}], [${ARGNAME}, ${TARGET}]]]
      //  ['R1', 'Anaphora', [['Anaphor', 'T2'], ['Entity', 'T1']]]
    ]
};
