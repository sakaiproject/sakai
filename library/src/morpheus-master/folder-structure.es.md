# Directorios y fichero dentro de la estructura de directorios

## Carpeta principal

El contenido base de los temas basados en la estructura de Morpheus se encuentra dentro de la carpeta `reference/library/src/morpheus-master` donde se puede encontrar tanto los ficheros SASS necesarios como las imágenes y los JavaScript del comportamiento.

Si queréis desarrollar un tema para Morpheus desde 0 únicamente hay que duplicar esta estructura y compilarlo a través de `mvn clean install sakai:deploy` en vuestra carpeta `library`

## Morpheus-master, el tema por defecto para Sakai 11.

Nos vamos a centrar en este documento en la carpeta `sass` que es la que tiene el contenido principal de clases e identificadores de la hoja de estilos. Vamos a ir repasando uno a uno los directorios incluidos en este directorio.

## Directorio inicial.

 - `_defaults.scss`, al tener un carácter `_` al principio del nombre no se compilará, es el lugar donde se encuentran todas las variables por defecto que se utilizan en el tema, como logotipo que usaremos, color de fondo, color de los enlaces o tipografía. Si queremos únicamente modificar estos colores pero no queremos perder el contenido que hemos descargado desde *git* sólo tenemos que crear un fichero `_customization.scss` que contendrá las variables modificadas. Tenemos varios ejemplos de este tipo de ficheros dentro de la carpeta `examples`
 - `access.scss`, se utiliza en la carga de las páginas de recursos para usuarios que no necesitan estar logueados, enlaces que han sido compartidos a usuarios de fuera de la plataforma.
 - `portal.scss`, deprecated, se está eliminando de todos los sitios donde aparece. Actualmente está vacío y se mantiene temporalmente para evitar errores 404 en la carga de Sakai
 - `print.scss`, son los estilos de impresion predeterminados cuando se imprime desde el propio navegador. Únicamente se carga su contenido en este caso con un `media="print"` en la cabecera del documento.
 - `tool.scss`, es el fichero principal del tema, el que después llamarán tanto el portal como cada una de las herramientas de Sakai. Dentro del mismo se incluyen las llamadas a cada uno de los ficheros parciales en los que está dividido Morpheus por legibilidad.
 
## Directorio base

En el directorio `base` podemos encontrar los estilos por defecto que se utilizarán en toda la plataforma, independientemente de las herramientas o funcionalidades que carguemos a través de `sakai.properties`. Vamos a ir revisando fichero por fichero para explicarlo con detenimiento por orden alfabético.

- `_bootstrap-defaults.scss`, al añadir la librería bootstrap ha habido que modificar, antes de su compilación desde SASS, algunas variables para mantener la consistencia entre tipografías y colores con el diseño de Morpheus. Si queréis personalizar vuestro propio tema, acordaos de modificar estas variables.
- `_compass.scss`, hasta hace poco, Morpheus funcionaba con JRuby + Compass. Al eliminar esta dependencia en la compilación era necesario añadir las funciones / mixins de compass que utilizábamos sobre todo relacionadas con CSS3 y su diferencia entre navegadores. Están aquí.
- `_defaults.scss`, son los estilos para las etiquetas HTML por defecto (`body`, links, `input`)
- `_extendables.scss`, son funciones creadas para el tema de morpheus que se utilizan en todo el tema, por ejemplo, la definición de los estilos para los botones (`.btn`) que se usan después para definirlos en `_defaults.scss` y que pueden ser utilizados también para los enlaces bien añadiendo la clase necesaria a nuestra etiqueta, bien usando `@extend` sobre cualquier clase (por ejemplo, un enlace específico, por `id`, actuando como botón).
- `_icons.scss`, es la definición de cada uno de los iconos vectoriales que Font Awesome provee. Se utilizan en el menú lateral y en el submenú de herramientas del menú de sitio, así como 
- `_responsive.scss`, en este caso, Morpheus utiliza, para abrir los menús en móvil, clases específicas en `body` para cada uno de los casos por separado, por si es necesario, tanto ahora como más adelante, dar estilos específicos en cualquier elemento con ese comportamiento. Es el único fichero un poco más enrevesado de entender y que necesita dos o tres vistazos en profundidad para entenderlo correctamente.
- `_rtl.scss`, provee el soporte para idiomas con escritura de derecha a izquierda como el árabe o el hebreo. Tiene en cuenta la forma de lectura del portal, cada desarrollador de una herramienta específica debería preocuparse de dar soporte asímismo, tal y como acertadamente `recursos` hace.

## Directorio modules

Como su propio nombre indica, hemos dividido el desarrollo de Morpheus compartimentando el diseño de cada una de las partes que conforman Sakai. Dentro de esta carpeta están cada uno de los módulos que hemos dividido y, dentro de ellos, el estilo específico o general para cada uno de ellos (navegación, migas de pan, el tutorial o la vista del alumno)

Como crear todas las carpetas para cada una de las herramientas eliminaría bastante legibilidad al directorio se han unificado todas dentro de la carpeta `tool`. Dentro de esta carpeta podéis encontrar una carpeta específica para cada herramienta y un fichero SASS (o más, los que sean necesarios) dentro de esa carpeta. Si vais a añadir uno, acordaos que **hay que añadirlo al fichero `tool.scss`** 

Además, para cada herramienta, existe un namespace creado para dar estilo específico si fuera necesario, de la forma "prefijo-nombreHerramienta". De esta forma, por ejemplo, Anuncios, tendrá su prefijo `Mrphs-sakai-announcements` que se traduce a SASS como `.#{$namespace}sakai-announcements`

Realmente, no debería ser necesario este namespace pero de momento lo es porque, al eliminar los `iframes` que contenían las herramientas muchas de ellas compartían clases para distintas funcionalidades.

Tienes algo más de detalle sobre cómo tratar una herramienta dentro de la estructura desde [aquí](customization-tool.md#what-should-i-do-with-a-tool).