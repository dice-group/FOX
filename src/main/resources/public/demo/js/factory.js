Fox.factory('JsonService', function($resource) {
	return $resource(
			'examples/:file',
			{ },
			{
				feedbackExp: {
					method:'GET', isArray: false, params: { file: 'feedbackExp.json'}},
				nerExp: {
					method:'GET', isArray: false, params: { file: 'nerExp.json'}},
        responseExp: {
					method:'GET', isArray: false, params: { file: 'response.json'}}
			}
	);
});
