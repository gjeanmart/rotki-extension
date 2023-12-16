
# === GENERAL ===

clean:
	rm -rf build/{css,js}/*

hard-refresh:
	rm -rf ./.shadow-cljs ./node_modules

install:
	make clean
	yarn install

# === CLJS ===

cljs/watch:
	npx shadow-cljs watch extension

cljs/release:
	npx shadow-cljs release extension


# === CSS ===

css/watch:
	npx tailwindcss \
		--input ./src/css/base.css \
		--output ./build/css/main.css \
		--watch

css/release:
	NODE_ENV=production
	npx tailwindcss \
		--input ./src/css/base.css \
		--output ./build/css/main.css \
		--minify

# === ALL ===

dev:
	make install
	npx concurrently \
		--kill-others \
		--prefix           "{time} [{name}]" \
		--timestamp-format "yyyy-MM-dd HH:mm:ss.SSS" \
		--prefix-colors    "bgBlue.bold,bgMagenta.bold" \
		--names             "css,cljs" \
		"make css/watch" \
		"make cljs/watch"

release:
# TODO check if jq is installed
# TODO add version as argument and update package.json and build/manifest.json
# TODO make environment (develpment, production) as argument
	$(eval PACKAGE_VERSION := $(shell cat ./package.json | jq -r '.version'))
	@echo Creating release v.$(PACKAGE_VERSION)
	make install
	make cljs/release
## This is trick to prevent "Uncaught SyntaxError: The requested module './shared.js' does not provide an export named '$jscomp'"	
	echo 'export var $$jscomp=$$jscomp;' >> build/js/shared.js
	make css/release
	mkdir -p ../dist/
	cd build ; \
		zip -r ../dist/rotki-extension-v$(PACKAGE_VERSION).zip * ; \
		cd ..