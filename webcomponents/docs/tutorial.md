# Sakai Webcomponents Tutorial #

In this tutorial we're going to learn how to:

1. Add a new sakai-todo-list component  to Sakai
2. Deploy a basic Sakai tool to host our new component
3. Work on our component directly in the deployed Tomcat webapp, refreshing the browser to see our changes
4. Internationalise the component using Sakai's i18n rest api
5. Add a rest endpoint to Sakai's web api and fetch our todos from it

You'll need a Sakai tomcat environment set up and ready to go. You'll need an editor - vim, emacs,
VS code, IntelliJ IDEA, Eclipse - any will do as we're just editing javascript.

Conventions used in this tutorial:

We'll use SAKAI\_SRC to denote the root directory for your Sakai source code. So,
SAKAI\_SRC/webcomponents will denote the webcomponents project in your Sakai source code.

The source code for this tutorial is [here](https://github.com/adrianfish/sakai-webcomponent-tutorial-src). Clone
it somewhere local to make it easier to just copy stuff out of it. Let's assume you've cloned that into
TUTORIAL\_SRC.

We'll use docs to describe this directory, the one containing this tutorial.

## 1. Add the sakai-todo-list component to Sakai

All of our components live under the SAKAI\_SRC/webcomponents/tool/src/main/fontend/js directory, that's
where we want to put our component's js file.

Copy sakai-todo-list.js to SAKAI\_SRC/webcomponents/tool/src/main/frontend/js

    cp TUTORIAL_SRC/js/sakai-todo-list.js ../tool/src/main/frontend/js

Now build the webcomponents project:

    cd SAKAI_SRC/webcomponents
    mvn clean install sakai:deploy

This will build and lint all the js files in the webcomponents project. They will be deployed to the
webcomponents webapp in your Tomcat. If you look in the webcomponents webapp in your Tomcat, you'll
see the sakai-todo-list.js file. Edit it and you see that it is more or less identical to your
source file. All that has happened is that the maven build has added a cache busting string to the
import paths, and the import paths have been adapted to work in the browser.

## 2. Add a tool to Sakai to host the todo list component

We now need to install a basic tool in Sakai to show our todo list component tag. This tutorial is
about the actual web component itself, so we've written the tool already. It's a Sakai project under
TUTORIAL\_SRC.

    cp -r TUTORIAL_SRC/java/todo-list-tool SAKAI_SRC
    cd SAKAI_SRC/todo-list-tool
    mvn clean install sakai:deploy

You should now be able to add the todo list tool to a site in your Sakai. When you click on the tool
you should just see "Sakai Todo List".

## 3. Do some work on the component, directly in the exploded Tomcat webapp directory

Go into your tomcat's webcomponents exploded webapp. You should see the sakai-todo-list.js file in
there. If you edit it, you'll see that the maven  build has altered it a bit from your source file.
Copy the sakai-todo-list.js source file into TOMCAT/webapps/webcomponents. You may have to set the
file permissions on your tomcat first.

    cp SAKAI_SRC/webcomponents/tool/src/main/frontend/js/sakai-todo-list.js TOMCAT/webapps/webcomponents

### 3.1 Add some styles

Now edit the file, in TOMCAT/webapps/webcomponents. Let's add some styles to the component. Alter the 
lit-element import line near the top of the file.

    import { css, html, LitElement } from "./assets/lit-element/lit-element.js";

Add this block of code near the bottom of the file, after the render function, but inside the class
definition. You can add it where you want in the class, but I prefer it at the bottom.

    static get styles() {

      return css`
        #title {
          font-weight: bold;
        }
      `;
    }

This is a static function on the SakaiTodoList class. It will generate a scoped, shared stylesheet for just this
component. Refresh the page in your browser and you should see the title go bold. That style you just added cannot
affect any other part of the sakai ui. It is scoped strictly to this component. Try adding some more styles to
that title id, and refreshing the browser to see the changes. The css keyword there is a tagged
template literal. It takes a template literal (text surrounded by backticks) and generates a scoped
stylesheet for this component only. Don't worry about it, it just works.

### 3.2 Add some properties

Properties are Lit's way of binding ui to data and they are set as a static member of the class. So,
add this at the top of the class, just after the declaration:

    class SakaiTodoList extends LitElement {

      constructor() {

        super();

        this.todos = [
          {
            what: "Buy some eggs",
            when: "Sat, 3rd June 2022",
          }
        ];
      }

      static get properties() {

        return {
          todos: { attribute: false, type: Array },
        };
      }

      ...

Each of these object properties is kept track of by Lit. When they change, any bound ui elements are
updated with the new value. For now, we're using the class constructor to initialise the data so we
see something - later we'll be retrieving the data from Sakai's webapi. Let's add a ui binding now:

    render() {

      return html`
        ...

        <table>
        ${this.todos.map(todo => html`
          <tr>
            <td>${todo.what}</td>
            <td>${todo.when}</td>
          </tr>
        `)}
        </table>

      `;
    }

Now refresh your browser. You should see the todo item in a table row. Now try adding this, just to
demonstrate the way Lit is watching your properties for changes. Add this to your constructor, just
after the todos initilisation:

    setInterval(() => {

      const now = new Date();
      this.todos.push({ what: now.getTime(), when: now.toLocaleString() });
      this.requestUpdate();
    }, 1000);

Now refresh and watch as the table is updated every second. The same thing would happen if we
fetched some data and requested an update, obviously a more realistic scenario than just changing
stuff in an interval!

You can add other properties to the object returned by get properties, and bind them in the render
function. The ui will be updated when those are changed, either by setting an attribute on the tag,
or some internal logic (a fetch, say) updating them. One thing worth noting here is that arrays don't
automatically update when the contents are altered. That's because Lit uses the value of the
property, and doesn't drill into object or array graphs. So, you call this.requestUpdate when you
change an array or an object's contents. Primitives like strings or numbers, they automatically update when
set. Alternatively, you can replace the array property to trigger the update. Up to you.

Delete that interval now, it was just a bit of fun. Refresh your browser and the updating should
stop.

## 4. Internationalisation

We use javascript class to handle our i18n in Sakai web components. We'll add some column headers to
our table and internationise them now. Add this to your imports:

    import { loadProperties } from "./sakai-i18n.js";

Update your properties function to look like this:

    static get properties() {

      return {
        i18n: { attribute: false, type: Object },
        todos: { attribute: false, type: Array },
      };
    }

Add this call to your constructor:

    constructor() {

      ...

      loadProperties("todo-list").then(r => this.i18n = r);

      ...
    }

Add some table header markup to your render function:

    html`
     ...
      <thead>
        <tr>
          <td>${this.i18n.what_header}</td>
          <td>${this.i18n.when_header}</td>
        </tr>
      </thead>
      ${this.todos.map(todo => html`
      ...
    `;

Refresh your browser and you'll see an error in the browser console (it's a good idea to have your
Chrome console open while working on this stuff). The error is telling us that the translation bundle
"todo-list" is not available yet.

Now is a great time to save what you've done back into your source tree from the deployed webapp. If
you forget to do this and run a maven build on webcomponents, **your changes will be lost!** So, do this:

    cp TOMCAT/webapps/webcomponents/sakai-todo-list.js SAKAI_SRC/webcomponents/tool/src/main/frontend/js

Okay, now we need to add our i18n bundle. Add a file to SAKAI\_SRC/webcomponents/bundle/src/main/bundle
called todo-list.properties. It's basically a Java properties file. Now add two properties:

    what_header=What
    when_header=When

Now build the webcomponents project:

    mvn clean install sakai:deploy

That will deploy the new strings to Sakai. Restart Tomcat and go into the tool and see if your strings
have been picked up by the todo list component. You may have to clear out your browser's session storage
as any i18n bundles are cached in there for performance reasons. They'll get refreshed when you reload
your stuff.

## 5. Add a rest endpoint to Sakai's web api and fetch our todos from it

Copy the controller class into the webapi sakai source.

    cp src/java/controller/TodoListController.java SAKAI_SRC/webapi/src/main/java/org/sakaiproject/webapi/controllers

Now build the webapi project with:

    cd SAKAI_SRC/webapi
    mvn clean install sakai:deploy

Now we can add the fetch call to the todo list component constructor:

    constructor() {

      super();

      const url = "/api/users/current/todos";
      fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Network error while fetching from ${url}`);
      })
      .then(todos => this.todos = todos)
      .catch(error => console.error(error));

      ...
    }

So, we create our url, then fetch, making sure to set credentials to "include" so the Sakai session
cookie is sent in the request. Fetch is by default asynchronous and returns promises. All we are
doing here is asynchronously pulling the todos from the endpoint we just built, and then setting
the "todos" property, thus triggering a render by Lit. Add some more data to the TodoListController
, rebuild the webapi project, refresh your browser and you'll see the new data.

