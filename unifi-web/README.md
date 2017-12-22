# Project setup

Requires node and npm and yarn to be installed on the local machine.

## Install steps

`yarn install` - installs all the dependencies of the project 

## Run steps

`SOCKET_URI=<vagrant socket ip with port incluted> yarn run` - runs the app on a localhost 3000 port

## Dev Workflow 

It helps a lot to see how components behave in isolation. That is why we have `storybook`, a tool
which allows us to document and demo react components easily. To run storybook:

`yarn storybook` 

Then navigate to the url provided in the terminal. You'll see a demo of all components added in there.
Development is actually very pleasent in storybook as it provides a clear way to test all props
of a component.

## Build steps

When building in a CI env or deploying for production we need to run the linter and the testing 
framework to check for problems before building.

- `yarn test` and `yarn lint` need to be run successfuly before building.
- `yarn build` will build the static assets into the `/build` folder. This folder should contain
all the required assets for serving the client side application. It will have a index.html as an entry
point and css and js files that will load from within. 

The `/build` folder needs to be servable. **We need to check if the imported css and js from the build
directory is relative to the location of the html or not**.

## Project strucutre

The frontend project is split into a flat structure. We try to split the concernes into simple drawers. One folder will contain data interaction components, another one simple reusable ui, another 
more specialised, project specific content, and so on. 

This allows us to focus sometimes on completely decoupled parts of the app (like reusable components)
without having to worry about app logic. Since react splits data and presentation extremely well this
approach works fine. 

The following folders are in the `/src` folder now:

- `elements`: contains the most basic form of reusable user interface components. Buttons, Layouts, Inputs, Dropdowns go here. This folder should be a plug and play between multiple projects. It should have 
no other dependecies than what is in `node_modules`

- `components`: contains more specialised user interface components. These are usually made up of html and components from the `elements` folder. For example, say we have a text input in the elements folder. In comonents we will have a field that would go into forms. The field has labels, error and success states and so on. In a similar way to elements, this folder should contain reusable components
that would work fine between projects. 

- `smart components`: contains business logic related user interface components. These are specific
to the applications. In here, the entities we work with are linked to the data layer. They assume a speccific project structure and require certain data configuration comming from the data layer to 
be available in order to function. A login form for example would be a good candidate for this folder. The login form is linked to the data via redux. It knows about the validation rules specific to this application. It might even have a method for submiting the data to the server. 

- `pages`: a special case of `smart components`, these entities are separated because it is more intuitive to see which route maps to what page. They are the configurations of each page in the app. 
The purpose of a page is to be hooked into a router and prepare children components to render data. They are also responsible for making sure that the data needed on the current page is available. 

- `reducers`: the redux data layer. Here we have delcaration on how the state tree looks like and what
actions can alter the tree. This layer is pure javascript and has nothing to do with the UI. 

- `utils`: helper functions, constants, math functions, anything that is used across the entire folder structure. 

- `lib`: a special case. Contains the web sockets library which might become a separate library/utility in the future. Think of what goes here as local node modules. 


### Working with pages

WIP

### Working with componnets

WIP

### Working with forms

WIP

### Data flow

WIP