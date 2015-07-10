'use strict';

Fox.DemoCtrl = function($routeParams, $scope, $http) {

    $scope.currentCtrl = 'DemoCtrl';
    $scope.isCollapsed = false;
    $scope.foxRequest = {
        type: 'text',
        task: 'ner',
        input: '',
        output: 'RDF/XML',
        nif: 0,
        foxlight: 'OFF'
    };
    $scope.foxRequest.defaults = 0;
    $scope.foxRequest.state = 'done';

    $scope.$watch('foxRequest.defaults', function(newValue, oldValue) {

        if (newValue == 3) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Berlin is an American New Wave band. Despite its name, Berlin did not have any known major connections with Germany, but instead was formed in Los Angeles, California in 1978.";
            $scope.foxRequest.output = 'Turtle';

        } else if (newValue == 2) {
            $scope.foxRequest.type = 'url';
            $scope.foxRequest.input = "http://es.wikipedia.org/wiki/Universidad_de_Leipzig";
            $scope.foxRequest.output = 'JSON-LD';

        } else if (newValue == 1) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Leipzig (en alemán estándar [ˈlaɪpʦɪç]) o Lipsia, en español, es una ciudad alemana en el noroeste del estado de Sajonia. Con más de 520 000 habitantes1 a finales de 2012 es la segunda ciudad más poblada de ese estado federado (tras Dresde, la capital de Sajonia) y tras Berlín, la tercera de todo el este de Alemania. Ya en el año 1165 recibió el derecho de ciudad y de mercado (ahora recinto ferial). Desde entonces, Leipzig se convirtió en uno de los centros de comercio más importantes de la Europa central. La ciudad tiene una larga tradición de recinto ferial y una de las ferias más antiguas (1190) de Europa. Junto con Fráncfort del Meno es Leipzig el centro histórico de la imprenta y el comercio. Además, cuenta con una de las universidades -tanto clásica como de música- más antiguas de Alemania. En las últimas décadas, Leipzig fue el centro de las manifestaciones de los lunes (Montagsdemonstrationen) de 1989, las cuales dieron el impulso necesario a la reunificación alemana. La ciudad de Leipzig tiene una gran tradición musical, la cual debe agradecer, entre otros, a Johann Sebastian Bach, Richard Wagner y Felix Mendelssohn. La Orquesta de la Gewandhaus y el Coro de Santo Tomás han sólo engrandecido la fama de la ciudad en el mundo de la música. Leipzig es la cuna de muchas personalidades famosas, tales como el filósofo y científico Gottfried Wilhelm Leibniz (1646), el compositor Richard Wagner (1813), el historiador de arte Nikolaus Pevsner (1902), el tipógrafo Jan Tschichold (1902), el pintor Max Beckmann o el socialista Karl Liebknecht (1871).";
            $scope.foxRequest.output = 'Turtle';

        } else if (newValue == 4) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "La Universidad de Leipzig (alemán Universität Leipzig), localizada en Leipzig en el Estado Libre de Sajonia, es la segunda más antigua de Alemania. Fue fundada el 2 de diciembre de 1409 por Federico I, el Elector de Sajonia y su hermano Guillermo II, margrave de Meißen, y al principio consistió de cuatro facultades. Actualmente, ha crecido a 14 facultades, y cuenta con alrededor de 26 000 estudiantes. Desde su inicio la universidad ha disfrutado poco más de 600 años de enseñanza e investigación ininterrumpidas. Posiblemente, la Facultad de Medicina sea la facultad más renombrada de la universidad.";
            $scope.foxRequest.output = 'JSON-LD';

        } else { // default 0 or not def.
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Gottfried Wilhelm Leibniz, a veces von Leibniz (Leipzig, 1 de julio de 1646 - Hannover, 14 de noviembre de 1716) fue un filósofo, lógico, matemático, jurista, bibliotecario y político alemán. Fue uno de los grandes pensadores de los siglos XVII y XVIII, y se le reconoce como El último genio universal. Realizó profundas e importantes contribuciones en las áreas de metafísica, epistemología, lógica, filosofía de la religión, así como en la matemática, física, geología, jurisprudencia e historia. Incluso Denis Diderot, el filósofo deísta francés del siglo XVIII, cuyas opiniones no podrían estar en mayor oposición a las de Leibniz, no podía evitar sentirse sobrecogido ante sus logros, y escribió en la Enciclopedia: Quizás nunca haya un hombre leído tanto, estudiado tanto, meditado más y escrito más que Leibniz... Lo que ha elaborado sobre el mundo, sobre Dios, la naturaleza y el alma es de la más sublime elocuencia. Si sus ideas hubiesen sido expresadas con el olfato de Platón, el filósofo de Leipzig no cedería en nada al filósofo de Atenas.";
            $scope.foxRequest.output = 'JSON-LD';
        }
    });

    $scope.foxResponse = {
        input: '',
        output: '',
        log: ''
    };

    $scope.foxRequest.send = function() {
        $scope.foxRequest.state = 'sending';

        // prepare request
        var foxRequest = angular.copy($scope.foxRequest);
        foxRequest.input = encodeURIComponent(foxRequest.input);

        // set fox parameter that we always use in the web demo
        foxRequest.returnHtml = true;

        // clear old data
        $scope.foxResponse.input = '';
        $scope.foxResponse.output = '';
        $scope.foxResponse.log = '';

        var method = 'POST';
        //var url = 'http://' + $scope.host + ':' + $scope.port + '/api'; 
        var url = 'http://' + $scope.host + '/api';
        var data = foxRequest;


        $http({
            method: method,
            url: url,
            data: data
        }).
        success(function(data, status, headers, config) {

            $scope.foxResponse.input = decodeURIComponent(data.input);
            $scope.foxResponse.output = decodeURIComponent(data.output);
            $scope.foxResponse.log = decodeURIComponent(data.log);

            $scope.foxRequest.state = 'done';
            $scope.isCollapsed = true;
        }).
        error(function(data, status, headers, config) {
            $scope.foxRequest.state = 'done';
        });
    };
};

Fox.controller('DemoCtrl', Fox.DemoCtrl);
