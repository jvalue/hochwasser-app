hochwasser-app
==============


** Configuring the ODS

This app requires an instance of the Open Data Service (ODS) to be running in order to download
flooding related data. As such the ODS needs to have a data source configured with the
following details:

- name: `pegelonline`
- domainIdKey: `/uuid`
- schema:
```json
{
  $schema: "http://json-schema.org/draft-04/schema#",
  type: "object",
  properties: {
    uuid: {
      type: "string"
    },
    number: {
      type: "string"
    },
    shortname: {
      type: "string"
    },
    longname: {
      type: "string"
    },
    km: {
      type: "number"
    },
    agency: {
      type: "string"
    },
    longitude: {
      type: "number"
    },
    latitude: {
      type: "number"
    },
    water: {
      type: "object",
      properties: {
        shortname: {
          type: "string"
        },
        longname: {
          type: "string"
        }
      }
    },
    timeseries: {
      type: "array",
      items: {
        type: "object",
        properties: {
          shortname: {
            type: "string"
          },
          longname: {
            type: "string"
          },
          unit: {
            type: "string"
          },
          equidistance: {
            type: "integer"
          },
          currentMeasurement: {
            type: "object",
            properties: {
              timestamp: {
                type: "string"
              },
              value: {
                type: "number"
              },
              trend: {
                type: "integer"
              },
              stateMnwMhw: {
                type: "string"
              },
              stateNswHsw: {
                type: "string"
              }
            }
          },
          gaugeZero: {
            type: "object",
            properties: {
              unit: {
                type: "string"
              },
              value: {
                type: "integer"
              },
              validFrom: {
                type: "string"
              }
            }
          },
          characteristicValues: {
            type: "array",
            items: {
              type: "object",
              properties: {
                shortname: {
                  type: "string"
                },
                longname: {
                  type: "string"
                },
                unit: {
                  type: "string"
                },
                value: {
                  type: "integer"
                },
                validFrom: {
                  type: "string"
                },
                timespanStart: {
                  type: "string"
                },
                timespanEnd: {
                  type: "string"
                },
                occurrences: {
                  type: "array",
                  items: {
                    type: "string"
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

** Configuring the CEPS

Besides the ODS, this app also requires an instance of the Complex Event Service (CEPS)
to be running handling alarms and notifications.

The CEPS needs to be configured by

- adding `pegelonline` source of the ODS
- adding a new EPL adapter called `pegelAlarm` with the following details:
  - eplBlueprint:
```json
select station1, station2 from pattern
[
    every station1=`pegelonline`
    (
        uuid = 'UUID'
        and timeseries.firstof(i => i.shortname = 'W') is not null
    )
    ->
    station2=`pegelonline`
    (
        UUID = 'UUID'
        and timeseries.firstof(i => i.shortname = 'W') is not null
    )
]
where
    station1.timeseries.firstof(i => i.shortname = 'W'
    and i.currentMeasurement.value <= LEVEL) is not null
    and
    station2.timeseries.firstof(i => i.shortname = 'W'
    and i.currentMeasurement.value > LEVEL) is not null
```
  - requiredArguments:
    - `UUID` of type `STRING`
    - `LEVEL` of type `NUMBER`
