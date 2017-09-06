// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012 Massachusetts Institute of Technology. All rights reserved.

/**
 * @license
 * @fileoverview Visual blocks editor for App Inventor
 * @author mckinney@mit.edu (Andrew F. McKinney)
 */

'use strict';

goog.provide('Blockly.language_switch');

goog.require('AI.Blockly.Msg.en');
goog.require('AI.Blockly.Msg.zh_cn');

Blockly.language_switch = {
  // Switch between languages
  switchLanguage: function (language) {
    if (Blockly.mainWorkspace) {
      var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
      Blockly.mainWorkspace.clear();
    }
    switch (language) {
      case 'en_US':
      case 'en':
        Blockly.Msg.en.switch_blockly_language_to_en.init();
        Blockly.Msg.en.switch_language_to_english.init();
        break;
      default:
        // 'zh_CN'
        Blockly.Msg.zh.hans.switch_blockly_language_to_zh_hans.init();
        Blockly.Msg.zh.switch_language_to_chinese_cn.init();
        break;
    }
    if (Blockly.mainWorkspace) {
      Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
    }
  }
};

//switch language before blocks are generated
var language = window.parent.__gwt_Locale;
// console.log("Language = " + language);
Blockly.language_switch.switchLanguage(language);
