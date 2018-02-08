// formula key: LIB_MD_DATA_ACCESS
// extends MD "namespace" by DataAccess "module"
var MD = (function (MD) {
    'use strict';

    var CLASSIFIER_TYPE = "#CLASSIFIER_TYPE";

    function define_library(){
        var bridge = simpleDataAccessService; // instance of SimpleDataAccessService implementation

        var module = {};

        module.lookupData = function(entityMetaKey, paramPair, dataActuality) {
        	if (!entityMetaKey) {
        		return null;
        	}
            return Java.from(bridge.lookupData(
        		currentCalculation.model.actuality,
        		typeof dataActuality === 'undefined' ? null : dataActuality,
				currentCalculation, currentProfile, entityMetaKey, paramPair
    		));
        };

        module.Calculation = {};

        module.Calculation.getActuality = function () {
            return currentCalculation.actuality;
        };

        module.Calculation.getDataActuality = function () {
            return currentCalculation.dataActuality;
        };

        module.Calculation.getModelActuality = function () {
            return currentCalculation.model.actuality;
        };

        module.Calculation.adjustDate = function(sourceDate, years, months, days) {
            if (years) {
                sourceDate = sourceDate.plusYears(years);
            }
            if (months) {
                sourceDate = sourceDate.plusMonths(months);
            }
            if (days) {
                sourceDate = sourceDate.plusDays(days);
            }
            return sourceDate;
        };

        module.Calculation.getCurrentCalculation = function () {
            return currentCalculation;
        };

        module.Calculation.getCurrentModel = function () {
            return currentCalculation.model;
        };
        
        /* shortcuts */
    	module.getClassifierValue = function(classifierKey, refClassifierAttributeKey, dataActuality) {
    		var classifierList = MD.DataAccess.lookupData(classifierKey, [], dataActuality);
    		if (classifierList != null && classifierList.length == 1 && classifierList[0] != null) {
    			if (refClassifierAttributeKey) {
    				var entityList = classifierList[0].getAttribute(classifierKey + CLASSIFIER_TYPE).getEntityList();
    				if (entityList != null && entityList.length == 1 && entityList[0] != null) {
    					return entityList[0].getAttributeValue(refClassifierAttributeKey);
    				}
    				return null;
    			}
    		    return classifierList[0].getAttributeValue(classifierKey + CLASSIFIER_TYPE);
    		}
    		return null;
    	};

    	module.getFormValue = function(formKey, formAttributeKey, filter, dataActuality) {
    		var formList = MD.DataAccess.lookupData(formKey, filter ? filter : [], dataActuality);
    		return MD.DataAccess.$innerValue(formAttributeKey, formList);
    	};

    	module.getDictionaryValue = function(dictionaryKey, dictionaryAttributeKey, filter, dataActuality) {
    		var dictionaryList = MD.DataAccess.lookupData(dictionaryKey, filter ? filter : [], dataActuality ? dataActuality : currentCalculation.dataActuality);
    		return MD.DataAccess.$innerValue(dictionaryAttributeKey, dictionaryList);
    	};

    	module.$innerValue = function(attributeKey, dataEntityList) {
    		var result = {};
    		if (dataEntityList && dataEntityList.length > 0) {
    			result.count = dataEntityList.length;
    			result.data = {};
    			for (var i = 0; i < dataEntityList.length; i++) {
    				if (dataEntityList[i].getAttribute(attributeKey).meta.type == 'REFERENCE') {
    					var collectedAttributes = new Array();
    					var entityList = dataEntityList[i].getAttribute(attributeKey).entityList;

    					for (var j = 0; j < entityList.length; j++) {
    						for (var attr in entityList[j].attributes) {
    							collectedAttributes[collectedAttributes.length] = entityList[j].getAttributeValue(attr);
    						}
    					}
    					result.data[i] = collectedAttributes;
    				} else {
    					result.data[i] = dataEntityList[i].getAttributeValue(attributeKey);
    				}
    			}
    		} else {
    			result.count = 0;
    			result.data = null;
    		}
			return result;
    	};

        return module;
    };

    MD.DataAccess = define_library();
    return MD;
})(MD || {});
