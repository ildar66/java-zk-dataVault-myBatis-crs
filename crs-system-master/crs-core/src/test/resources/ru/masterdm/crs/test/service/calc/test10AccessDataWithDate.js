var actuality = MD.DataAccess.Calculation.getActuality(); /*jvm LocalDateTime*/

var list1 = MD.DataAccess.lookupData("CLASSIFIER01", [{"KEY": "DATAACCD0101"}, {"CLASSIFIER01#DATETIME": actuality}]);
var e1 = list1[0];

var list2 = MD.DataAccess.lookupData("CLASSIFIER01", [{"KEY": "DATAACCD0102"}, {"CLASSIFIER01#DATETIME": actuality}]);
var e2 = list2[0];

e1.getAttributeValue("CLASSIFIER01#NUMBER") + e2.getAttributeValue("CLASSIFIER01#NUMBER");