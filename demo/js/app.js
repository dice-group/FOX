'use strict';
var Fox = angular.module('Fox',['ngResource', 'ui.bootstrap', 'ui']);
Fox.pages =      ['home', 'demo', 'doc'];
Fox.pagesCtrl =  ['HomeCtrl', 'DemoCtrl', 'DocCtrl'];
Fox.config(['$routeProvider', '$locationProvider', function($routeProvider,$locationProvider) {
    angular.forEach(Fox.pages , function(value, key){
        $routeProvider.when('/' + value, {
            templateUrl: 'partials/' + value + '.html',
            controller: Fox.pagesCtrl[key]
        });
    });
    $routeProvider.otherwise({
        redirectTo: '/' + Fox.pages[0]
    });
    $locationProvider.html5Mode(false).hashPrefix('!');
}]);
Fox.run(function($rootScope,$window) {
    
    $rootScope.pages = Fox.pages;
    
    $rootScope.host  = $window.location.host;
    $rootScope.port = $window.location.port;
});
