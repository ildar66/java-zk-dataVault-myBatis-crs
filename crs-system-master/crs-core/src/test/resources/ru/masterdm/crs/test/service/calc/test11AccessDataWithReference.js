var list1 = MD.DataAccess.lookupData("CLASSIFIER01R", [{"KEY": "DATAACCR01R01"}, {"CLASSIFIER01R#NUMBER": 10},
                                                       {"CLASSIFIER01R#CLASS02": [
                                                           {"CLASSIFIER02#NUMBER": 10},
                                                           {"CLASSIFIER02#STRING": "string value DATAACCR0201"}]
                                                       }]);
var e1 = list1[0];

var list2 = MD.DataAccess.lookupData("CLASSIFIER01R", [{"KEY": "DATAACCR01R02"}, {"CLASSIFIER01R#NUMBER": 20},
                                                        {"CLASSIFIER01R#CLASS02": [
                                                            {"CLASSIFIER02#NUMBER": 10},
                                                            {"CLASSIFIER02#STRING": "string value DATAACCR0201"}]
                                                        }]);
var e2 = list2[0];

e1.getAttributeValue("CLASSIFIER01R#NUMBER") + e2.getAttributeValue("CLASSIFIER01R#NUMBER");