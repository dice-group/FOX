Fox.directive("ngBrat", function($compile) {
      return {
            //restrict: 'E',
            //replace: true,
            //template: '<div>A:{{internalControl}}</div>',
            scope: {
              setfn: '&'
            },
            link: function(scope, element, attrs){
               	scope.updateMap = function() {

                  //$newDirective =  element.clone(true);
                  $par = element.children();
                  //console.log($par);
                  if( $par.hasClass('hasSVG')){
                     $par.attr( 'class' , "" );
                     $par.attr( 'style' , "" );
                     $par.children().remove();
                     $par.children().remove();
                     $par.children().remove();
                     $par.children().remove();
                     //$compile(element)(scope);
                  }
                };
                 //element.bind("mouseover",function() {
                   //console.log(element);
                 //});
                 scope.setfn({theDirFn: scope.updateMap});
               }
            };
});
