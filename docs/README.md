# AuroraGuis Documentation

This is the complete documentation for AuroraGuis - a modern, powerful GUI library for Spigot/Paper plugins.

## Documentation Structure

### Getting Started
- **[Installation](getting-started/installation.md)** - Add AuroraGuis to your project
- **[Quick Start](getting-started/quick-start.md)** - Create your first GUI in 5 minutes
- **[Basic Concepts](getting-started/concepts.md)** - Learn the fundamentals

### Core Features
- **[Basic GUIs](features/basic-guis.md)** - Standard event-based GUIs
- **[Packet GUIs](features/packet-guis.md)** - Anti-duplication protection
- **[Resource Pack Support](features/resource-packs.md)** - Custom models and pixel-perfect titles
- **[Config-Based GUIs](features/config-guis.md)** - YAML configuration with auto-commands
- **[Virtual GUIs](features/virtual-guis.md)** - Extended sizes beyond 54 slots
- **[Animations](features/animations.md)** - Built-in animation system
- **[Conditions & Cooldowns](features/conditions-cooldowns.md)** - Click validation

### Guides & Tutorials
- **[Creating a Shop](guides/shop-tutorial.md)** - Step-by-step shop tutorial
- **[Resource Pack Setup](guides/resource-pack-setup.md)** - Complete resource pack guide
- **[Config System Guide](guides/config-system.md)** - YAML configuration deep dive
- **[Anti-Dupe Setup](guides/anti-dupe.md)** - Securing your economy
- **[Performance Tips](guides/performance.md)** - Optimization best practices

### API Reference
- **[AuroraGui API](api/aurora-gui.md)** - Event-based GUI class
- **[PacketGui API](api/packet-gui.md)** - Packet-based GUI class
- **[VirtualGui API](api/virtual-gui.md)** - Extended size GUI class
- **[Builders](api/builders.md)** - GuiBuilder, VirtualGuiBuilder, etc.
- **[Utilities](api/utilities.md)** - ItemBuilder, ColorUtils, etc.

### Examples
- **[Code Examples](examples/code-examples.md)** - Ready-to-use snippets
- **[YAML Examples](examples/yaml-examples.md)** - Configuration examples
- **[Complete Projects](examples/complete-projects.md)** - Full implementations

## Quick Links

- [GitHub Repository](https://github.com/Unforgoten1/AuroraGuis)
- [Issue Tracker](https://github.com/Unforgoten1/AuroraGuis/issues)
- [Discussions](https://github.com/Unforgoten1/AuroraGuis/discussions)
- [Changelog](https://github.com/Unforgoten1/AuroraGuis/blob/master/CHANGELOG.md)

## Building Documentation Website

This documentation is formatted for use with static site generators like MkDocs or Docusaurus.

### Using MkDocs

1. Install MkDocs:
```bash
pip install mkdocs mkdocs-material
```

2. Create `mkdocs.yml` in project root:
```yaml
site_name: AuroraGuis Documentation
theme:
  name: material
  palette:
    primary: indigo
  features:
    - navigation.tabs
    - navigation.sections
    - toc.integrate

nav:
  - Home: index.md
  - Getting Started:
      - Installation: getting-started/installation.md
      - Quick Start: getting-started/quick-start.md
      - Concepts: getting-started/concepts.md
  - Features:
      - Basic GUIs: features/basic-guis.md
      - Packet GUIs: features/packet-guis.md
      - Resource Packs: features/resource-packs.md
      - Config GUIs: features/config-guis.md
      - Virtual GUIs: features/virtual-guis.md
      - Animations: features/animations.md
  - API Reference:
      - AuroraGui: api/aurora-gui.md
      - PacketGui: api/packet-gui.md
      - VirtualGui: api/virtual-gui.md
      - Builders: api/builders.md
      - Utilities: api/utilities.md
  - Examples:
      - Code Examples: examples/code-examples.md
      - YAML Examples: examples/yaml-examples.md
```

3. Build and serve:
```bash
mkdocs serve
# Open http://127.0.0.1:8000
```

4. Build for production:
```bash
mkdocs build
# Output in site/ directory
```

### Using Docusaurus

1. Install Docusaurus:
```bash
npx create-docusaurus@latest aurora-docs classic
```

2. Copy docs to `docs/` folder in Docusaurus project

3. Update `docusaurus.config.js` with navigation

4. Run development server:
```bash
npm start
```

## Contributing to Documentation

Contributions are welcome! To contribute:

1. Fork the repository
2. Make your changes to the documentation
3. Test locally with MkDocs or Docusaurus
4. Submit a pull request

### Documentation Standards

- Use clear, concise language
- Include code examples for all features
- Add YAML examples where applicable
- Keep line length under 100 characters
- Use proper markdown formatting
- Test all code examples

## License

Documentation is licensed under MIT License, same as the project.

---

**Version:** 1.1.0
**Last Updated:** February 2026
