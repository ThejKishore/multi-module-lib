```
preference:
    language: kotlin
    buildTool: gradle
    framework: Javalin 6.7.0
    libraries: JDBI 3.+
               Koin
               H2
               Hoplite
               HikariCP
               openapi
               html-dsl
               htmx
               picocss
```

### Create Config server and store the data in the db.
### DB
Table Name: Properties
| column Name | Column Type |
| --- | --- |
| Application Name | Varchar |
| Profile | Varchar |
| Label | Varchar |
| Property Key  | Varchar |
| Property Value  | Varchar |


### Have admin screen developed in the html-dsl and htmx
### Have endpoints the server the properties for an application in the format of yaml.
### Have UI in HTMX that allows the user to view all config for the application/profile/label or /application/profile in the ui.
### Adding a new property to the existing application using the htmx ui
### Modifying the existing property using the htmx ui
### Removing the existing property using the htmx ui



