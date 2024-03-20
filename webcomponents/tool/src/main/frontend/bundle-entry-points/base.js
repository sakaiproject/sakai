import "@sakai-ui/sakai-date-picker/sakai-date-picker.js";
import "@sakai-ui/sakai-user-photo/sakai-user-photo.js";
import "imagesloaded";
import "@sakai-ui/sakai-profile/sakai-profile.js";
import "@sakai-ui/sakai-pronunciation-player/sakai-pronunciation-player.js";
import "@sakai-ui/sakai-picture-changer/sakai-picture-changer.js";
import "@sakai-ui/sakai-notifications/sakai-notifications.js";

import Sortable from "sortablejs";
globalThis.Sortable = Sortable;

import { loadProperties, tr } from "@sakai-ui/sakai-i18n";
globalThis.loadProperties = loadProperties;
globalThis.tr = tr;

