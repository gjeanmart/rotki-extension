
## Rotki extension


A Chrome extension for [Rotki opensource portfolio tracker](https://rotki.com/) ([Github](https://github.com/rotki/rotki)), the extension connects to Rotki backend API (running locally or remotely) and provides basic insights about your portfolio.

<img src='https://i.ibb.co/Ny58NMM/Screenshot-at-Dec-11-10-42-06-1.png' width='200'>

### Features
- [X] Home: Assets list & balances, and net total
- [X] Settings: Configure your Rotki backend endpoint, timeout, snapshot TTL and more
- [X] Background worker: Data are refreshed and cached automatically by the service worker every 15 minutes (configurable)
- [X] Offline access: Data and icons are cached by the extension, allowing access to the last state without having Rotki backend API running
- [X] Extension icon reflects portfolio trend over the past 7 days

### Roadmap
- [ ] New page to display the accounts list
- [ ] Multi-currency support (convert USD value to EUR, GBP, etc.)
- [ ] Multi-language support
- [ ] Insert a button on Etherscan's account page to add an account to Rotki address book straight from Etherscan (and potentially other websites)
- [ ] Improve light theme and implement dark theme
- [ ] Release pipeline (increment version, tag, package)

### Technologies

- **[Clojure(Script)](https://clojurescript.org)**: ClojureScript is a compiler for Clojure that targets JavaScript.
- **[Shadow-cljs](https://github.com/thheller/shadow-cljs)**: ClojureScript compiler and JS bundler
- **[Re-frame](https://day8.github.io/re-frame/)** & **[Reagent](https://reagent-project.github.io/)**: A framework for building Modern Web Apps in ClojureScript. It leverages React.
- **[TailwindCSS](https://tailwindcss.com/)**: CSS framework
- **[Daisy UI](https://daisyui.com/)**: Tailwind CSS component library
- **[Chart.js](https://www.chartjs.org/)**: JavaScript charting library
- **[Phosphor-icons](https://phosphoricons.com/)**: All icons you need
- **[Tempura (i18n)](https://github.com/taoensso/tempura)**: Simple text localization for Clojure(Script) applications

### Contribute

#### Requirements

- **[Node.js](https://nodejs.org)** (v6.0.0+, most recent version preferred)
- **[Yarn](https://www.yarnpkg.com)**
- **[Java SDK](https://adoptium.net/)** (Version 11+, Latest LTS Version recommended)

#### Getting started

- Run `$ make dev`

#### Run local extension

- Go to <a href="chrome://extensions">chrome://extensions/</a>
- Enable "Developer mode" (top-right corner)
- Click on "Load unpacked" and select the folder `build`

### Tests

- Run `$ make test`

### Release on Chrome store

- Increment version in `package.json` & `build/manifest.json`
- Run `$ make release`
- Go to [chrome.google.com/webstore/devconsole](https://chrome.google.com/webstore/devconsole) and upload new package (from the folder `dist`)