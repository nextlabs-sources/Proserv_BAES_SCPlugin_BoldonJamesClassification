INSERT INTO ACTION_PLUGINS (ID, DISPLAY_ORDER, CLASS_NAME, NAME, DISPLAY_NAME, DESCRIPTION, FIRE_ONCE_PER_RULE,REPOSITORY_TYPE)
 VALUES (1609141800000001, 1, 'com.nextlabs.smartclassifier.plugin.action.removeboldonjamesclassification.RemoveBoldonJamesClassification', 'REMOVE_BOLDON_JAMES_CLASSIFICATION', 'Remove Boldon James Classification', 'Removes Boldon James Classification', 0 ,'SHARED FOLDER');

INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1609141800000001, 1609141800000001, 1, 'P', 0, 0, 'String', 'Boldon James Configuration File (spif.xml)', 'spif_path', 1, 'C:\spif.xml');
 
INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER)
 VALUES (1609141800000002, 1609141800000001, 2, 'P', 1, 0, 'String', 'Labels to Remove', 'spif_security_category_tag_set', 0);

INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1609141800000003, 1609141800000001, 3, 'P', 0, 0, 'String', 'Power Classifier Installation Home', 'power_classifier_path', 1, 'C:\Program Files\Boldon James\Power Classifier for Files');
 
INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1609141800000004, 1609141800000001, 4, 'P', 0, 0, 'String', 'Exclude Folder', 'exclude_folder', 1, '');
 
 INSERT INTO ACTION_PLUGINS (ID, DISPLAY_ORDER, CLASS_NAME, NAME, DISPLAY_NAME, DESCRIPTION, FIRE_ONCE_PER_RULE,REPOSITORY_TYPE)
 VALUES (1610191500000001, 1, 'com.nextlabs.smartclassifier.plugin.action.addboldonjamesclassification.AddBoldonJamesClassification', 'ADD_BOLDON_JAMES_CLASSIFICATION', 'Add Boldon James Classification', 'Adds Boldon James Classification', 0,'SHARED FOLDER');

INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1610191500000001, 1610191500000001, 1, 'P', 0, 0, 'String', 'Boldon James Configuration File (spif.xml)', 'spif_path', 1, 'C:\spif.xml');
 
INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER)
 VALUES (1610191500000002, 1610191500000001, 2, 'P', 1, 0, 'String', 'Labels to Add', 'spif_security_category_tag_set', 0);

INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1610191500000003, 1610191500000001, 3, 'P', 0, 0, 'String', 'Power Classifier Installation Home', 'power_classifier_path', 1, 'C:\Program Files\Boldon James\Power Classifier for Files');
 
INSERT INTO ACTION_PLUGIN_PARAMS (ID, ACTION_PLUGIN_ID, DISPLAY_ORDER, PARAM_TYPE, COLLECTIONS, KEY_VALUE, DATA_TYPE, LABEL, IDENTIFIER, FIXED_PARAMETER, FIXED_VALUE)
 VALUES (1610191500000004, 1610191500000001, 4, 'P', 0, 0, 'String', 'Exclude Folder', 'exclude_folder', 1, '');

UPDATE SYSTEM_CONFIGS SET VALUE='id file_id directory folder_url document_name repository_type file_type creation_date last_modified_date author last_author site_url serverrelativeurl_t repository_path last_modified_date_millisecond record_category_t last_modified_date record_activity_t record_declaration_date_t record_declaration_date_tdt' WHERE IDENTIFIER ='solr.indexing.query.ruleEngineFields';