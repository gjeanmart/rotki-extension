
## Rotki extension


A Chrome extension for [Rotki opensource portfolio tracker](https://rotki.com/) ([Github](https://github.com/rotki/rotki)), the extension connects to Rotki backend API (running locally or remotely) and provides basic insights about your portfolio.

<img src='https://i.ibb.co/S3yXFch/Screenshot-at-Dec-08-20-48-44.png' width='200'>

### Features
- [X] Home: Assets list & balances, and net total
- [X] Settings: Configure your Rotki backend endpoint, timeout, snapshot TTL and more
- [X] Background worker: Data are refreshed and cached automatically by the service worker evert 15 minutes (configuration TTL)
- [X] Offline access: Data and icons are cached by extension, allowing access to the last state without having Rotki backend API running

### Roadmap
- [ ] Add badge ^ (up) or v (down) to reflect the last 7d or 1m or 6m or 1y portfolio valuation trend
- [ ] New page to display the accounts list
- [ ] Force refresh option
- [ ] Multi-currency support (convert USD value to EUR, GBP, etc.)
- [ ] Insert a button on Etherscan's account page to add an account to Rotki address book straight from Etherscan (and potentially other websites)
- [ ] Improve light theme and implement dark theme
- [ ] Release to Chrome store

### Technologies

- [Clojure(Script)](https://clojurescript.org/)
- [Shadow-cljs](https://github.com/thheller/shadow-cljs)
- [Re-frame](https://day8.github.io/re-frame/) & [Reagent](https://reagent-project.github.io/)
- [Tailwing](https://tailwindcss.com/)
- [Daisy UI](https://daisyui.com/)
- [Chart.js](https://www.chartjs.org/)
- [Phosphor-icons](https://phosphoricons.com/)
- [Tempura (i18n)](https://github.com/taoensso/tempura)

### Contribute

#### Requirements

- [node.js](https://nodejs.org) (v6.0.0+, most recent version preferred)
- [yarn](https://www.yarnpkg.com)
- [Java SDK](https://adoptium.net/) (Version 11+, Latest LTS Version recommended)

#### Getting started

```
$ make run/dev
```

#### Upload unpacked

- Open `chrome://extensions/`
- Enable "Developer mode" (top-right corner)
- Click on "Load unpacked" and select the folder `<workspace>/build`


### Release

- Increment version in `package.json`
- Run `$ make release`
- Upload new package (in `dist/`)