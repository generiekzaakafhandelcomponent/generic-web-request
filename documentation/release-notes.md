# Release notes

Overzicht van wijzigingen per versie van de Generic Web Request plugin.

## 0.0.1

- Eerste versie: actie `send-request` verstuurt een HTTP-verzoek met methode, pad, headers en body uit de proceskoppeling.
- Headers en bodyvelden zijn key/value-paren; de body wordt als JSON-object verstuurd.
- Elke waarde is letterlijke tekst, een verwijzing (`pv:`, `doc:`, `case:`) of tekst met verwijzingen erin, zoals
  `https://example.com/pv:id`. Een verwijzing als hele waarde behoudt haar type.
- Authenticatie via de HTTP Client Authentication-plugin (3.1.1).
- De taak faalt bij een onoplosbare verwijzing, een dubbel veld, een onbereikbare server of een niet-2xx-antwoord.
