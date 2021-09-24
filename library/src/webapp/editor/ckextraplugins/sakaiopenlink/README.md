# Sakai Open Link plugin

Forked from <https://github.com/mlewand/ckeditor-plugin-openlink>

## Sakai configuration

The plugin is enabled by default `config.sakaiOpenLink = false` will disable the plugin.

The **Open Link** is a very simple plugin, extending context menu with a possibility to open link in a new tab.

* extending context menu with a possibility to open link in a new tab,
* allowing you to open link with a ctrl/cmd click,

It also integrates with linked [image2](http://ckeditor.com/addon/image2) widgets.

## Browser Compatibility

Basically the same as [CKEditor](http://docs.ckeditor.com/#!/guide/dev_browsers) with one exception: opening a link with ctrl click does not work in Internet Explorer / Edge browsers. Pull requests are welcome.

## Config Options

There are few config options available, you need to define them in standard [CKEditor config](http://docs.ckeditor.com/#!/guide/dev_configuration) object.

### `config.openlink_modifier`

Specifies what modifier key(s) should be pressed to open the link. It's based on `CKEDITOR.CTRL`, `CKEDITOR.SHIFT` and `CKEDITOR.ALT` members.

You might also provide `0` as the value - it will cause any click to open the link, without need to press any modifier key.

You can specify multiple modifiers, e.g. having `config.openlink_modifier = CKEDITOR.SHIFT + CKEDITOR.CTRL` would trigger the link with CTRL key or SHIFT key or both of them being pressed.

Defaults to: `CKEDITOR.CTRL`

### `config.openlink_enableReadOnly`

Determines whether this plugin feature should be available also in read-only mode. For backward compatibility reason this value is set to `false` by default.

```
config.openlink_enableReadOnly = true; // Allows opening links also while editor is in read-only mode.
```

Defaults to: `false`

### `config.openlink_target`

Sets the target where new window should be open.

```
config.openlink_target = '_self'; // Will cause current page to be replaced by link click.
```

Defaults to: `'_blank'`

## Installation

See the official [Plugin Installation Guide](http://docs.ckeditor.com/#!/guide/dev_plugins).
