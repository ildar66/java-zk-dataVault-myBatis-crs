var list1 = MD.DataAccess.lookupData("CLASSIFIER01", [{"KEY": "DATAACC0101"}, {"CLASSIFIER01#NUMBER": 10}]);
var e1 = list1[0];

var list2 = MD.DataAccess.lookupData("CLASSIFIER01", [{"KEY": "DATAACC0102"}, {"CLASSIFIER01#NUMBER": 20}]);
var e2 = list2[0];

e1.getAttributeValue("CLASSIFIER01#NUMBER") + e2.getAttributeValue("CLASSIFIER01#NUMBER");