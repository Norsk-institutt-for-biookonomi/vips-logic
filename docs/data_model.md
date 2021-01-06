#Data model
## Organisms (crops, pests, diseases and weeds)
Crops, pests, diseases and weeds are all organisms subject to [classification](https://en.wikipedia.org/wiki/Taxonomy_(biology)#Modern_system_of_classification). In VIPS, they are all placed in the organisms table. Following the rules of the classification system, the table is a tree structure. So each node (bar the root/top nodes) have a parent organism - which may be a family, genus etc.

### Crop categories
Each organization (NIBIO, Jordbruksverket, ADAS etc.) has their own set of crop categories. They can be e.g. cereals, fruit, vegetables. Each of these links to one or more organisms.