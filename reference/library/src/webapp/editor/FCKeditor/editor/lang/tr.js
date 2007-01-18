/*
 * FCKeditor - The text editor for internet
 * Copyright (C) 2003-2006 Frederico Caldeira Knabben
 * 
 * Licensed under the terms of the GNU Lesser General Public License:
 * 		http://www.opensource.org/licenses/lgpl-license.php
 * 
 * For further information visit:
 * 		http://www.fckeditor.net/
 * 
 * "Support Open Source software. What about a donation today?"
 * 
 * File Name: tr.js
 * 	Turkish language file.
 * 
 * File Authors:
 * 		Bogac Guven (bogacmx@yahoo.com)
 */

var FCKLang =
{
// Language direction : "ltr" (left to right) or "rtl" (right to left).
Dir					: "ltr",

ToolbarCollapse		: "Araç Çubugunu Kapat",
ToolbarExpand		: "Araç Çubugunu Aç",

// Toolbar Items and Context Menu
Save				: "Kaydet",
NewPage				: "Yeni Sayfa",
Preview				: "Ön İzleme",
Cut					: "Kes",
Copy				: "Kopyala",
Paste				: "Yapıştır",
PasteText			: "Düzyazı Olarak Yapıştır",
PasteWord			: "Word'den Yapıştır",
Print				: "Yazdır",
SelectAll			: "Tümünü Seç",
RemoveFormat		: "Biçimi Kaldır",
InsertLinkLbl		: "Köprü",
InsertLink			: "Köprü Ekle/Düzenle",
RemoveLink			: "Köprü Kaldır",
Anchor				: "Çapa Ekle/Düzenle",
InsertImageLbl		: "Resim",
InsertImage			: "Resim Ekle/Düzenle",
InsertFlashLbl		: "Flash",
InsertFlash			: "Flash Ekle/Düzenle",
InsertTableLbl		: "Tablo",
InsertTable			: "Tablo Ekle/Düzenle",
InsertLineLbl		: "Satır",
InsertLine			: "Yatay Satır Ekle",
InsertSpecialCharLbl: "Özel Karakter",
InsertSpecialChar	: "Özel Karakter Ekle",
InsertSmileyLbl		: "İfade",
InsertSmiley		: "İfade Ekle",
About				: "FCKeditor Hakkında",
Bold				: "Kalın",
Italic				: "İtalik",
Underline			: "Altı Çizgili",
StrikeThrough		: "Üstü Çizgili",
Subscript			: "Alt Simge",
Superscript			: "Üst Simge",
LeftJustify			: "Sola Dayalı",
CenterJustify		: "Ortalanmış",
RightJustify		: "Sağa Dayalı",
BlockJustify		: "İki Kenara Yaslanmış",
DecreaseIndent		: "Sekme Azalt",
IncreaseIndent		: "Sekme Arttır",
Undo				: "Geri Al",
Redo				: "Tekrarla",
NumberedListLbl		: "Numaralı Liste",
NumberedList		: "Numaralı Liste Ekle/Kaldır",
BulletedListLbl		: "Simgeli Liste",
BulletedList		: "Simgeli Liste Ekle/Kaldır",
ShowTableBorders	: "Tablo Kenarlarını Göster",
ShowDetails			: "Detayları Göster",
Style				: "Stil",
FontFormat			: "Biçim",
Font				: "Yazı Tipi",
FontSize			: "Boyut",
TextColor			: "Yazı Rengi",
BGColor				: "Arka Renk",
Source				: "Kaynak",
Find				: "Bul",
Replace				: "Değiştir",
SpellCheck			: "Yazım Denetimi",
UniversalKeyboard	: "Evrensel Klavye",
PageBreakLbl		: "Sayfa sonu",
PageBreak			: "Sayfa Sonu Ekle",

Form			: "Form",
Checkbox		: "Onay Kutusu",
RadioButton		: "Seçenek Düğmesi",
TextField		: "Metin Girişi",
Textarea		: "Çok Satırlı Metin",
HiddenField		: "Gizli Veri",
Button			: "Düğme",
SelectionField	: "Seçim Mönüsü",
ImageButton		: "Resimli Düğme",

FitWindow		: "Editör boyutunu büyüt",

// Context Menu
EditLink			: "Köprü Düzenle",
CellCM				: "Hücre",
RowCM				: "Satır",
ColumnCM			: "Sütun",
InsertRow			: "Satır Ekle",
DeleteRows			: "Satır Sil",
InsertColumn		: "Sütun Ekle",
DeleteColumns		: "Sütun Sil",
InsertCell			: "Hücre Ekle",
DeleteCells			: "Hücre Sil",
MergeCells			: "Hücreleri Birleştir",
SplitCell			: "Hücre Böl",
TableDelete			: "Tabloyu Sil",
CellProperties		: "Hücre Özellikleri",
TableProperties		: "Tablo Özellikleri",
ImageProperties		: "Resim Özellikleri",
FlashProperties		: "Flash Özellikleri",

AnchorProp			: "Çapa Özellikleri",
ButtonProp			: "Düğme Özellikleri",
CheckboxProp		: "Onay Kutusu Özellikleri",
HiddenFieldProp		: "Gizli Veri Özellikleri",
RadioButtonProp		: "Seçenek Düğmesi Özellikleri",
ImageButtonProp		: "Resimli Düğme Özellikleri",
TextFieldProp		: "Metin Girişi Özellikleri",
SelectionFieldProp	: "Seçim Mönüsü Özellikleri",
TextareaProp		: "Çok Satırlı Metin Özellikleri",
FormProp			: "Form Özellikleri",

FontFormats			: "Normal;Biçimli;Adres;Başlık 1;Başlık 2;Başlık 3;Başlık 4;Başlık 5;Başlık 6;Paragraf (DIV)",

// Alerts and Messages
ProcessingXHTML		: "XHTML işleniyor. Lütfen bekleyin...",
Done				: "Bitti",
PasteWordConfirm	: "Yapıştırdığınız yazı Word'den gelmişe benziyor. Yapıştırmadan önce gereksiz eklentileri silmek ister misiniz?",
NotCompatiblePaste	: "Bu komut Internet Explorer 5.5 ve ileriki sürümleri için mevcuttur. Temizlenmeden yapıştırılmasını ister misiniz ?",
UnknownToolbarItem	: "Bilinmeyen araç çubugu öğesi \"%1\"",
UnknownCommand		: "Bilinmeyen komut \"%1\"",
NotImplemented		: "Komut uyarlanamadı",
UnknownToolbarSet	: "\"%1\" araç çubuğu öğesi mevcut değil",
NoActiveX			: "Kullandığınız tarayıcının güvenlik ayarları bazı özelliklerin kullanılmasını engelliyor. Bu özelliklerin çalışması için \"Run ActiveX controls and plug-ins (Activex ve eklentileri çalıştır)\" seçeneğinin aktif yapılması gerekiyor. Kullanılamayan eklentiler ve hatalar konusunda daha fazla bilgi sahibi olun.",
BrowseServerBlocked : "Kaynak tarayıcısı açılamadı. Tüm \"popup blocker\" programlarının devre dışı olduğundan emin olun. (Yahoo toolbar, Msn toolbar, Google toolbar gibi)",
DialogBlocked		: "diyalog açmak mümkün olmadı. Tüm \"Popup Blocker\" programlarının devre dışı olduğundan emin olun.",

// Dialogs
DlgBtnOK			: "Tamam",
DlgBtnCancel		: "İptal",
DlgBtnClose			: "Kapat",
DlgBtnBrowseServer	: "Sunucuyu Gez",
DlgAdvancedTag		: "Gelişmiş",
DlgOpOther			: "<Diğer>",
DlgInfoTab			: "Bilgi",
DlgAlertUrl			: "Lütfen URL girin",

// General Dialogs Labels
DlgGenNotSet		: "<tanımlanmamış>",
DlgGenId			: "Kimlik",
DlgGenLangDir		: "Lisan Yönü",
DlgGenLangDirLtr	: "Soldan Sağa (LTR)",
DlgGenLangDirRtl	: "Sağdan Sola (RTL)",
DlgGenLangCode		: "Lisan Kodlaması",
DlgGenAccessKey		: "Erişim Tuşu",
DlgGenName			: "İsim",
DlgGenTabIndex		: "Sekme İndeksi",
DlgGenLongDescr		: "Uzun Tanımlı URL",
DlgGenClass			: "Stil Klasları",
DlgGenTitle			: "Danışma Baslığı",
DlgGenContType		: "Danışma İçerik Türü",
DlgGenLinkCharset	: "Bağlı Kaynak Karakter Gurubu",
DlgGenStyle			: "Stil",

// Image Dialog
DlgImgTitle			: "Resim Özellikleri",
DlgImgInfoTab		: "Resim Bilgisi",
DlgImgBtnUpload		: "Sunucuya Yolla",
DlgImgURL			: "URL",
DlgImgUpload		: "Karsıya Yükle",
DlgImgAlt			: "Alternatif Yazı",
DlgImgWidth			: "Genişlik",
DlgImgHeight		: "Yükseklik",
DlgImgLockRatio		: "Oranı Kilitle",
DlgBtnResetSize		: "Boyutu Başa Döndür",
DlgImgBorder		: "Kenar",
DlgImgHSpace		: "Yatay Boşluk",
DlgImgVSpace		: "Dikey Boşluk",
DlgImgAlign			: "Hizalama",
DlgImgAlignLeft		: "Sol",
DlgImgAlignAbsBottom: "Tam Altı",
DlgImgAlignAbsMiddle: "Tam Ortası",
DlgImgAlignBaseline	: "Taban Çizgisi",
DlgImgAlignBottom	: "Alt",
DlgImgAlignMiddle	: "Orta",
DlgImgAlignRight	: "Sağ",
DlgImgAlignTextTop	: "Yazı Tepeye",
DlgImgAlignTop		: "Tepe",
DlgImgPreview		: "Ön İzleme",
DlgImgAlertUrl		: "Lütfen resmin URL'sini yazınız",
DlgImgLinkTab		: "Köprü",

// Flash Dialog
DlgFlashTitle		: "Flash Özellikleri",
DlgFlashChkPlay		: "Otomatik Oynat",
DlgFlashChkLoop		: "Döngü",
DlgFlashChkMenu		: "Flash Mönüsünü Kullan",
DlgFlashScale		: "Boyutlandır",
DlgFlashScaleAll	: "Hepsini Göster",
DlgFlashScaleNoBorder	: "Kenar Yok",
DlgFlashScaleFit	: "Tam Sığdır",

// Link Dialog
DlgLnkWindowTitle	: "Köprü",
DlgLnkInfoTab		: "Köprü Bilgisi",
DlgLnkTargetTab		: "Hedef",

DlgLnkType			: "Köprü Türü",
DlgLnkTypeURL		: "URL",
DlgLnkTypeAnchor	: "Bu sayfada çapa",
DlgLnkTypeEMail		: "E-Posta",
DlgLnkProto			: "Protokol",
DlgLnkProtoOther	: "<diğer>",
DlgLnkURL			: "URL",
DlgLnkAnchorSel		: "Çapa Seç",
DlgLnkAnchorByName	: "Çapa İsmi ile",
DlgLnkAnchorById	: "Eleman Id ile",
DlgLnkNoAnchors		: "<Bu dokümanda hiç çapa yok>",
DlgLnkEMail			: "E-Posta Adresi",
DlgLnkEMailSubject	: "Mesaj Konusu",
DlgLnkEMailBody		: "Mesaj Vücudu",
DlgLnkUpload		: "Karşıya Yükle",
DlgLnkBtnUpload		: "Sunucuya Gönder",

DlgLnkTarget		: "Hedef",
DlgLnkTargetFrame	: "<çerçeve>",
DlgLnkTargetPopup	: "<yeni açılan pencere>",
DlgLnkTargetBlank	: "Yeni Pencere(_blank)",
DlgLnkTargetParent	: "Anne Pencere (_parent)",
DlgLnkTargetSelf	: "Kendi Penceresi (_self)",
DlgLnkTargetTop		: "En Üst Pencere (_top)",
DlgLnkTargetFrameName	: "Hedef Çerçeve İsmi",
DlgLnkPopWinName	: "Yeni Açılan Pencere İsmi",
DlgLnkPopWinFeat	: "Yeni Açılan Pencere Özellikleri",
DlgLnkPopResize		: "Boyutlandırılabilir",
DlgLnkPopLocation	: "Yer Çubuğu",
DlgLnkPopMenu		: "Mönü Çubuğu",
DlgLnkPopScroll		: "Kaydırma Çubukları",
DlgLnkPopStatus		: "Statü Çubuğu",
DlgLnkPopToolbar	: "Araç Çubuğu",
DlgLnkPopFullScrn	: "Tam Ekran (IE)",
DlgLnkPopDependent	: "Bağlı-Dependent- (Netscape)",
DlgLnkPopWidth		: "Genişlik",
DlgLnkPopHeight		: "Yükseklik",
DlgLnkPopLeft		: "Sola Göre Pozisyon",
DlgLnkPopTop		: "Yukarıya Göre Pozisyon",

DlnLnkMsgNoUrl		: "Lütfen köprü URL'sini yazın",
DlnLnkMsgNoEMail	: "Lütfen E-posta adresini yazın",
DlnLnkMsgNoAnchor	: "Lütfen bir çapa seçin",
DlnLnkMsgInvPopName	: "The popup name must begin with an alphabetic character and must not contain spaces",	//MISSING

// Color Dialog
DlgColorTitle		: "Renk Seç",
DlgColorBtnClear	: "Temizle",
DlgColorHighlight	: "Belirle",
DlgColorSelected	: "Seçilmiş",

// Smiley Dialog
DlgSmileyTitle		: "İfade Ekle",

// Special Character Dialog
DlgSpecialCharTitle	: "Özel Karakter Seç",

// Table Dialog
DlgTableTitle		: "Tablo Özellikleri",
DlgTableRows		: "Satırlar",
DlgTableColumns		: "Sütunlar",
DlgTableBorder		: "Kenar Kalınlığı",
DlgTableAlign		: "Hizalama",
DlgTableAlignNotSet	: "<Tanımlanmamış>",
DlgTableAlignLeft	: "Sol",
DlgTableAlignCenter	: "Merkez",
DlgTableAlignRight	: "Sağ",
DlgTableWidth		: "Genişlik",
DlgTableWidthPx		: "piksel",
DlgTableWidthPc		: "yüzde",
DlgTableHeight		: "Yükseklik",
DlgTableCellSpace	: "Izgara kalınlığı",
DlgTableCellPad		: "Izgara yazı arası",
DlgTableCaption		: "Başlık",
DlgTableSummary		: "Özet",

// Table Cell Dialog
DlgCellTitle		: "Hücre Özellikleri",
DlgCellWidth		: "Genişlik",
DlgCellWidthPx		: "piksel",
DlgCellWidthPc		: "yüzde",
DlgCellHeight		: "Yükseklik",
DlgCellWordWrap		: "Sözcük Kaydır",
DlgCellWordWrapNotSet	: "<Tanımlanmamış>",
DlgCellWordWrapYes	: "Evet",
DlgCellWordWrapNo	: "Hayır",
DlgCellHorAlign		: "Yatay Hizalama",
DlgCellHorAlignNotSet	: "<Tanımlanmamış>",
DlgCellHorAlignLeft	: "Sol",
DlgCellHorAlignCenter	: "Merkez",
DlgCellHorAlignRight: "Sağ",
DlgCellVerAlign		: "Dikey Hizalama",
DlgCellVerAlignNotSet	: "<Tanımlanmamış>",
DlgCellVerAlignTop	: "Tepe",
DlgCellVerAlignMiddle	: "Orta",
DlgCellVerAlignBottom	: "Alt",
DlgCellVerAlignBaseline	: "Taban Çizgisi",
DlgCellRowSpan		: "Satır Kapla",
DlgCellCollSpan		: "Sütun Kapla",
DlgCellBackColor	: "Arka Plan Rengi",
DlgCellBorderColor	: "Kenar Rengi",
DlgCellBtnSelect	: "Seç...",

// Find Dialog
DlgFindTitle		: "Bul",
DlgFindFindBtn		: "Bul",
DlgFindNotFoundMsg	: "Belirtilen yazı bulunamadı.",

// Replace Dialog
DlgReplaceTitle			: "Değiştir",
DlgReplaceFindLbl		: "Aranan:",
DlgReplaceReplaceLbl	: "Bununla değiştir:",
DlgReplaceCaseChk		: "Büyük/küçük harf duyarlı",
DlgReplaceReplaceBtn	: "Değiştir",
DlgReplaceReplAllBtn	: "Tümünü Değiştir",
DlgReplaceWordChk		: "Kelimenin tamamı uysun",

// Paste Operations / Dialog
PasteErrorPaste	: "Gezgin yazılımınızın güvenlik ayarları editörün otomatik yapıştırma işlemine izin vermiyor. İşlem için (Ctrl+V) tuşlarını kullanın.",
PasteErrorCut	: "Gezgin yazılımınızın güvenlik ayarları editörün otomatik kesme işlemine izin vermiyor. İşlem için (Ctrl+X) tuşlarını kullanın.",
PasteErrorCopy	: "Gezgin yazılımınızın güvenlik ayarları editörün otomatik kopyalama işlemine izin vermiyor. İşlem için (Ctrl+C) tuşlarını kullanın.",

PasteAsText		: "Düz Metin Olarak Yapıştır",
PasteFromWord	: "Word'den yapıştır",

DlgPasteMsg2	: "Lütfen aşağıdaki kutunun içine yapıştırın. (<STRONG>Ctrl+V</STRONG>) ve <STRONG>Tamam</STRONG> butonunu tıklayın.",
DlgPasteIgnoreFont		: "Yazı Tipi tanımlarını yoksay",
DlgPasteRemoveStyles	: "Sitil Tanımlarını çıkar",
DlgPasteCleanBox		: "Temizlik Kutusu",

// Color Picker
ColorAutomatic	: "Otomatik",
ColorMoreColors	: "Diğer renkler...",

// Document Properties
DocProps		: "Doküman Özellikleri",

// Anchor Dialog
DlgAnchorTitle		: "Çapa Özellikleri",
DlgAnchorName		: "Çapa İsmi",
DlgAnchorErrorName	: "Lütfen çapa için isim giriniz",

// Speller Pages Dialog
DlgSpellNotInDic		: "Sözlükte Yok",
DlgSpellChangeTo		: "Şuna değiştir:",
DlgSpellBtnIgnore		: "Yoksay",
DlgSpellBtnIgnoreAll	: "Tümünü Yoksay",
DlgSpellBtnReplace		: "Değiştir",
DlgSpellBtnReplaceAll	: "Tümünü Değiştir",
DlgSpellBtnUndo			: "Geri Al",
DlgSpellNoSuggestions	: "- Öneri Yok -",
DlgSpellProgress		: "Yazım denetimi işlemde...",
DlgSpellNoMispell		: "Yazım denetimi tamamlandı: Yanlış yazıma rastlanmadı",
DlgSpellNoChanges		: "Yazım denetimi tamamlandı: Hiçbir kelime değiştirilmedi",
DlgSpellOneChange		: "Yazım denetimi tamamlandı: Bir kelime değiştirildi",
DlgSpellManyChanges		: "Yazım denetimi tamamlandı: %1 kelime değiştirildi",

IeSpellDownload			: "Yazım denetimi yüklenmemiş. Şimdi yüklemek ister misiniz?",

// Button Dialog
DlgButtonText		: "Metin (Değer)",
DlgButtonType		: "Tip",
DlgButtonTypeBtn	: "Button",	//MISSING
DlgButtonTypeSbm	: "Submit",	//MISSING
DlgButtonTypeRst	: "Reset",	//MISSING

// Checkbox and Radio Button Dialogs
DlgCheckboxName		: "İsim",
DlgCheckboxValue	: "Değer",
DlgCheckboxSelected	: "Seçili",

// Form Dialog
DlgFormName		: "İsim",
DlgFormAction	: "İşlem",
DlgFormMethod	: "Metod",

// Select Field Dialog
DlgSelectName		: "İsim",
DlgSelectValue		: "Değer",
DlgSelectSize		: "Boyut",
DlgSelectLines		: "satır",
DlgSelectChkMulti	: "Çoklu seçime izin ver",
DlgSelectOpAvail	: "Mevcut Seçenekler",
DlgSelectOpText		: "Metin",
DlgSelectOpValue	: "Değer",
DlgSelectBtnAdd		: "Ekle",
DlgSelectBtnModify	: "Düzenle",
DlgSelectBtnUp		: "Yukari",
DlgSelectBtnDown	: "Aşağı",
DlgSelectBtnSetValue : "Seçili değer olarak ata",
DlgSelectBtnDelete	: "Sil",

// Textarea Dialog
DlgTextareaName	: "İsim",
DlgTextareaCols	: "Sütunlar",
DlgTextareaRows	: "Satırlar",

// Text Field Dialog
DlgTextName			: "İsim",
DlgTextValue		: "Değer",
DlgTextCharWidth	: "Karakter Genişliği",
DlgTextMaxChars		: "En Fazla Karakter",
DlgTextType			: "Tip",
DlgTextTypeText		: "Metin",
DlgTextTypePass		: "Şifre",

// Hidden Field Dialog
DlgHiddenName	: "İsim",
DlgHiddenValue	: "Değer",

// Bulleted List Dialog
BulletedListProp	: "Simgeli Liste Özellikleri",
NumberedListProp	: "Numaralı Liste Özellikleri",
DlgLstStart			: "Start",	//MISSING
DlgLstType			: "Tip",
DlgLstTypeCircle	: "Çember",
DlgLstTypeDisc		: "Disk",
DlgLstTypeSquare	: "Kare",
DlgLstTypeNumbers	: "Sayılar (1, 2, 3)",
DlgLstTypeLCase		: "Küçük Harfler (a, b, c)",
DlgLstTypeUCase		: "Büyük Harfler (A, B, C)",
DlgLstTypeSRoman	: "Küçük Romen Rakamları (i, ii, iii)",
DlgLstTypeLRoman	: "Büyük Romen Rakamları (I, II, III)",

// Document Properties Dialog
DlgDocGeneralTab	: "Genel",
DlgDocBackTab		: "Arka Plan",
DlgDocColorsTab		: "Renkler ve Mesafeler",
DlgDocMetaTab		: "Tanım Bilgisi (Meta)",

DlgDocPageTitle		: "Sayfa Başlığı",
DlgDocLangDir		: "Lisan Yönü",
DlgDocLangDirLTR	: "Soldan Sağa (LTR)",
DlgDocLangDirRTL	: "Sağdan Sola (RTL)",
DlgDocLangCode		: "Lisan Kodu",
DlgDocCharSet		: "Karakter Kümesi Kodlaması",
DlgDocCharSetCE		: "Central European",	//MISSING
DlgDocCharSetCT		: "Chinese Traditional (Big5)",	//MISSING
DlgDocCharSetCR		: "Cyrillic",	//MISSING
DlgDocCharSetGR		: "Greek",	//MISSING
DlgDocCharSetJP		: "Japanese",	//MISSING
DlgDocCharSetKR		: "Korean",	//MISSING
DlgDocCharSetTR		: "Turkish",	//MISSING
DlgDocCharSetUN		: "Unicode (UTF-8)",	//MISSING
DlgDocCharSetWE		: "Western European",	//MISSING
DlgDocCharSetOther	: "Diğer Karakter Kümesi Kodlaması",

DlgDocDocType		: "Doküman Türü Başlığı",
DlgDocDocTypeOther	: "Diğer Doküman Türü Başlığı",
DlgDocIncXHTML		: "XHTML Bildirimlerini Dahil Et",
DlgDocBgColor		: "Arka Plan Rengi",
DlgDocBgImage		: "Arka Plan Resim URLsi",
DlgDocBgNoScroll	: "Sabit Arka Plan",
DlgDocCText			: "Metin",
DlgDocCLink			: "Köprü",
DlgDocCVisited		: "Görülmüs Köprü",
DlgDocCActive		: "Aktif Köprü",
DlgDocMargins		: "Kenar Boşlukları",
DlgDocMaTop			: "Tepe",
DlgDocMaLeft		: "Sol",
DlgDocMaRight		: "Sağ",
DlgDocMaBottom		: "Alt",
DlgDocMeIndex		: "Doküman İndeksleme Anahtar Kelimeleri (virgülle ayrılmış)",
DlgDocMeDescr		: "Doküman Tanımı",
DlgDocMeAuthor		: "Yazar",
DlgDocMeCopy		: "Telif",
DlgDocPreview		: "Ön İzleme",

// Templates Dialog
Templates			: "Düzenler",
DlgTemplatesTitle	: "İçerik Düzenleri",
DlgTemplatesSelMsg	: "Editörde açmak için lütfen bir düzen seçin.<br>(hali hazırdaki içerik kaybolacaktır.):",
DlgTemplatesLoading	: "Düzenler listesi yüklenmekte. Lütfen bekleyiniz...",
DlgTemplatesNoTpl	: "(Belirli bir düzen seçilmedi)",
DlgTemplatesReplace	: "Replace actual contents",	//MISSING

// About Dialog
DlgAboutAboutTab	: "Hakkında",
DlgAboutBrowserInfoTab	: "Gezgin Bilgisi",
DlgAboutLicenseTab	: "Lisans",
DlgAboutVersion		: "versiyon",
DlgAboutLicense		: "GNU Kısıtlı Kamu Lisansı (LGPL) koşulları altında lisanslanmıştır",
DlgAboutInfo		: "Daha fazla bilgi için:"
}