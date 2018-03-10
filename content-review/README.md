## Content Review

The content-review project adds plagiarism integrations to sakai that can be used in other tools like assignments but can also be useful in other tools like forums or in tests and quiz's (i.e. essay style questions). In actuality plagiarism detection will become more standard in more places.

### Integrations

Currently there are 4 integrations with built in support:
- [VeriCite](impl/vericite/README.md)
- [Turnitin](impl/turnitin/README.md)
- [Urkund](impl/urkund/readme.md)
- [Compilatio](impl/compilatio/README.md)

Each integration has its own properties to configure.

### Content Review Federated Provider

The federated provider enables providers and also selects a default provider. OOTB a no op provider is configured until a provider is configured.

To enable providers:
contentreview.enabledProviders=VeriCite,Turnitin,Urkund,Compilatio

When multiple providers are configured it is ideal to select the default otherwise one is selected for you:
contentreview.defaultProvider=VeriCite

The log will show what providers are configured
```
...ContentReviewFederatedServiceImpl - Found Content Review Provider: VeriCite with providerId of 1930781763
...ContentReviewFederatedServiceImpl - Found Content Review Provider: Turnitin with providerId of 199481773
...ContentReviewFederatedServiceImpl - Enabled Content Review Provider: VeriCite with providerId of 1930781763
...ContentReviewFederatedServiceImpl - Enabled Content Review Provider: Turnitin with providerId of 199481773
...ContentReviewFederatedServiceImpl - Default Content Review Provider: Turnitin with providerId of 199481773
```

The providerId identifies the provider that was used to queue items in the CONTENTREVIEW_ITEM table.

