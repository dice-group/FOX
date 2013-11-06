Fox.factory('JsonService', function($resource) {
	return $resource(
			':file',
			{ }, 
			{
				feedbackExp: {method:'GET', isArray: false, params: { file: 'feedbackExp.json' } },
				nerExp: {method:'GET', isArray: false, params: { file: 'nerExp.json' } },
			}
	);
});
