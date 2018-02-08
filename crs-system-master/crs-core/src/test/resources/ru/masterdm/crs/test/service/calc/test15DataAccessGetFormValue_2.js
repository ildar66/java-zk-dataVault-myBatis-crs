var res = MD.DataAccess.getFormValue(formKey, formAttributeKey, [], dataActuality);
res.count == 1 ? res.data[0] : null;

