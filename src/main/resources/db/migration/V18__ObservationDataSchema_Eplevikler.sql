INSERT INTO public.observation_data_schema
(organization_id, organism_id, data_schema, data_model)
VALUES (1, 68,
        '{
          "$schema": "http://json-schema.org/draft-04/schema#",
          "type": "object",
          "title": " ",
          "properties": {
            "trap1": {
              "title": "Antall insekter, felle 1",
              "type": "number",
              "minimum": 0,
              "default": 0
            },
            "trap2": {
              "title": "Antall insekter, felle 2",
              "type": "number",
              "minimum": 0,
              "default": 0
            },
            "trap3": {
              "title": "Antall insekter, felle 3",
              "type": "number",
              "minimum": 0,
              "default": 0
            },
            "trap4": {
              "title": "Antall insekter, felle 4",
              "type": "number",
              "minimum": 0,
              "default": 0
            },
            "bbch": {
              "title": "BBCH",
              "type": "number",
              "minimum": 0,
              "default": 0
            }
          }
        }',
        '{
            "trap1":0,
            "trap2":0,
            "trap3":0,
            "trap4":0,
            "bbch":0
        }'
       );