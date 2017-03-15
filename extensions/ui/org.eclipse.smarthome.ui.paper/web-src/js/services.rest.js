angular.module('PaperUI.services.rest', [ 'PaperUI.constants' ]).config(function($httpProvider, restConfig) {
    var accessToken = function getAccessToken() {
        return $('#authentication').data('access-token')
    }();
    if (accessToken != '{{ACCESS_TOKEN}}') {
        var authorizationHeader = function getAuthorizationHeader() {
            return 'Bearer ' + accessToken
        }();
        $httpProvider.defaults.headers.common['Authorization'] = authorizationHeader;
    }

    var getQueryParams = function(qs) {
        qs = qs.split('+').join(' ');

        var params = {}, tokens, re = /[?&]?([^=]+)=([^&]*)/g;

        while (tokens = re.exec(qs)) {
            params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
        }

        return params;
    }
    var query = getQueryParams(document.location.search);
    var apiKey = query.api_key
    restConfig.apiKey = apiKey;
    if (restConfig.eventPath.indexOf('?') == -1) {
        restConfig.eventPath += '?api_key=' + apiKey;
    } else {
        restConfig.eventPath += '&api_key=' + apiKey;
    }

}).factory('itemService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/items', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/items?api_key=' + restConfig.apiKey + '&recursive=false'
        },
        getByName : {
            method : 'GET',
            params : {
                bindingId : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName?api_key=' + restConfig.apiKey
        },
        remove : {
            method : 'DELETE',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName?api_key=' + restConfig.apiKey
        },
        create : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName?api_key=' + restConfig.apiKey,
            transformResponse : function(response, headerGetter, status) {
                var response = angular.fromJson(response);
                if (status == 405) {
                    response.customMessage = "Item is not editable.";
                }
                return response;
            }
        },
        updateState : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName/state?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        getItemState : {
            method : 'GET',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName/state?api_key=' + restConfig.apiKey,
            transformResponse : function(data) {
                return data;
            },
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        sendCommand : {
            method : 'POST',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        addMember : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName?api_key=' + restConfig.apiKey
        },
        removeMember : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName?api_key=' + restConfig.apiKey
        },
        addTag : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag?api_key=' + restConfig.apiKey
        },
        removeTag : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag?api_key=' + restConfig.apiKey
        }
    });
}).factory('bindingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/bindings', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/bindings?api_key=' + restConfig.apiKey
        },
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/bindings/:id/config?api_key=' + restConfig.apiKey
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/bindings/:id/config?api_key=' + restConfig.apiKey
        },
    });
}).factory('inboxService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/inbox', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/inbox?api_key=' + restConfig.apiKey,
            transformResponse : function(data) {
                var results = angular.fromJson(data);
                for (var i = 0; i < results.length; i++) {
                    results[i].bindingType = results[i].thingTypeUID.split(':')[0];
                }
                return results
            },
        },
        approve : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/approve?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        ignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/ignore?api_key=' + restConfig.apiKey
        },
        unignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/unignore?api_key=' + restConfig.apiKey
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID?api_key=' + restConfig.apiKey
        }
    })
}).factory('discoveryService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/discovery', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/discovery?api_key=' + restConfig.apiKey
        },
        scan : {
            method : 'POST',
            params : {
                bindingId : '@bindingId'
            },
            transformResponse : function(data) {
                return {
                    timeout : angular.fromJson(data)
                }
            },
            url : restConfig.restPath + '/discovery/bindings/:bindingId/scan?api_key=' + restConfig.apiKey
        }
    });
}).factory('thingTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/thing-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/thing-types?api_key=' + restConfig.apiKey
        },
        getByUid : {
            method : 'GET',
            params : {
                thingTypeUID : '@thingTypeUID'
            },
            url : restConfig.restPath + '/thing-types/:thingTypeUID?api_key=' + restConfig.apiKey
        }
    });
}).factory('linkService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/links', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/links?api_key=' + restConfig.apiKey
        },
        link : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID?api_key=' + restConfig.apiKey
        },
        unlink : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID?api_key=' + restConfig.apiKey
        }
    });
}).factory('thingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/things', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/things?api_key=' + restConfig.apiKey
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID?api_key=' + restConfig.apiKey
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID?api_key=' + restConfig.apiKey
        },
        add : {
            method : 'POST',
            url : restConfig.restPath + '/things?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        update : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        updateConfig : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/config?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        link : {
            method : 'POST',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'
            },
            url : restConfig.restPath + '/things/:thingUID/channels/:channelId/link?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        unlink : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'
            },
            url : restConfig.restPath + '/things/:thingUID/channels/:channelId/link?api_key=' + restConfig.apiKey,
        }
    });
}).factory('serviceConfigService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/services', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/services?api_key=' + restConfig.apiKey
        },
        getById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id?api_key=' + restConfig.apiKey
        },
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/services/:id/config?api_key=' + restConfig.apiKey
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config?api_key=' + restConfig.apiKey
        },
        deleteConfig : {
            method : 'DELETE',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config?api_key=' + restConfig.apiKey
        },
    });
}).factory('configDescriptionService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/config-descriptions', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/config-descriptions?api_key=' + restConfig.apiKey
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@uri'
            },
            transformResponse : function(response, headerGetter, status) {
                var response = angular.fromJson(response);
                if (status == 404) {
                    response.showError = false;
                }
                return response;
            },
            url : restConfig.restPath + '/config-descriptions/:uri?api_key=' + restConfig.apiKey
        },
    });
}).factory('extensionService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/extensions', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/extensions?api_key=' + restConfig.apiKey
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@id'
            },
            url : restConfig.restPath + '/extensions/:id?api_key=' + restConfig.apiKey
        },
        getAllTypes : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/extensions/types?api_key=' + restConfig.apiKey
        },
        install : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/install?api_key=' + restConfig.apiKey
        },
        uninstall : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/uninstall?api_key=' + restConfig.apiKey
        }
    });
}).factory('ruleService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/rules', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/rules?api_key=' + restConfig.apiKey
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID?api_key=' + restConfig.apiKey
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            },
            url : restConfig.restPath + '/rules?api_key=' + restConfig.apiKey
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID?api_key=' + restConfig.apiKey
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script?api_key=' + restConfig.apiKey
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        update : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        setEnabled : {
            method : 'POST',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/enable?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        getRuleTemplates : {
            method : 'GET',
            url : restConfig.restPath + '/templates?api_key=' + restConfig.apiKey,
            isArray : true
        },
        runRule : {
            method : 'POST',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/runnow?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        }
    });
}).factory('moduleTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/module-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/module-types?api_key=' + restConfig.apiKey
        },
        getByType : {
            method : 'GET',
            params : {
                mtype : '@mtype'
            },
            url : restConfig.restPath + '/module-types?type=:mtype?api_key=' + restConfig.apiKey,
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID?api_key=' + restConfig.apiKey
        },
        getModuleConfigByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID',
                moduleCategory : '@moduleCategory',
                id : '@id'

            },
            url : restConfig.restPath + '/rules/:ruleUID/:moduleCategory/:id/config?api_key=' + restConfig.apiKey
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID?api_key=' + restConfig.apiKey
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script?api_key=' + restConfig.apiKey
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script?api_key=' + restConfig.apiKey,
            headers : {
                'Content-Type' : 'text/plain'
            }
        }
    });
}).factory('channelTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/channel-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/channel-types?api_key=' + restConfig.apiKey
        },
        getByUri : {
            method : 'GET',
            params : {
                channelTypeUID : '@channelTypeUID'
            },
            url : restConfig.restPath + '/channel-types/:channelTypeUID?api_key=' + restConfig.apiKey
        },
    });
}).factory('templateService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/channel-types', {}, {
        getAll : {
            method : 'GET',
            url : restConfig.restPath + '/templates',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                templateUID : '@templateUID'
            },
            url : restConfig.restPath + '/templates/:templateUID'
        },
    });
}).factory('imageService', function(restConfig, $http) {
    return {
        getItemState : function(itemName) {
            var promise = $http.get(restConfig.restPath + "/items/" + itemName + "/state").then(function(response) {
                return response.data;
            });
            return promise;
        }
    }
});