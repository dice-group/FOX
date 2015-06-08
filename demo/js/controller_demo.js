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
            $scope.foxRequest.input = "https://es.wikipedia.org/wiki/Universidad_de_Leipzig";
            $scope.foxRequest.output = 'JSON-LD';

        } else if (newValue == 1) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Colonia (en alemán: Ltspkr.png Köln; en kölsch: Kölle; en neerlandés: Keulen) es la cuarta ciudad más grande de Alemania, precedida por Berlín, Hamburgo y Múnich, y la más poblada dentro del Estado federado de Renania del Norte-Westfalia, aunque Düsseldorf es la capital del Estado. Fundada en el año 38 a. C. como Oppidum Ubiorum (Ciudad de los Ubios), fue posteriormente declarada colonia romana con el nombre de Colonia Claudia Ara Agrippinensium en alusión a la emperatriz Agripina, esposa del emperador Claudio y madre de Nerón.";
            $scope.foxRequest.output = 'Turtle';

        } else if (newValue == 4) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.";
            $scope.foxRequest.output = 'JSON-LD';

        } else { // default 0 or not def.
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Gottfried Wilhelm Leibniz, a veces von Leibniz1 (Leipzig, 1 de julio de 1646 - Hannover, 14 de noviembre de 1716) fue un filósofo, lógico, matemático, jurista, bibliotecario y político alemán. Fue uno de los grandes pensadores de los siglos XVII y XVIII, y se le reconoce como 'El último genio universal'. Realizó profundas e importantes contribuciones en las áreas de metafísica, epistemología, lógica, filosofía de la religión, así como en la matemática, física, geología, jurisprudencia e historia. Incluso Denis Diderot, el filósofo deísta francés del siglo XVIII, cuyas opiniones no podrían estar en mayor oposición a las de Leibniz, no podía evitar sentirse sobrecogido ante sus logros, y escribió en la Enciclopedia: 'Quizás nunca haya un hombre leído tanto, estudiado tanto, meditado más y escrito más que Leibniz... Lo que ha elaborado sobre el mundo, sobre Dios, la naturaleza y el alma es de la más sublime elocuencia. Si sus ideas hubiesen sido expresadas con el olfato de Platón, el filósofo de Leipzig no cedería en nada al filósofo de Atenas.'";
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
