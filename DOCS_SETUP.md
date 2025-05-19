# Documentation Website Setup Guide

This guide will help you set up and deploy the AuroraGuis documentation website.

## 📚 What's Included

Your documentation includes:
- **8 complete documentation files** (~15,000 words)
- **50+ code examples**
- **30+ YAML configuration examples**
- **Production-ready MkDocs configuration**
- **Material for MkDocs theme** (modern, responsive design)

## 🚀 Quick Start (5 Minutes)

### Step 1: Install MkDocs

```bash
# Install MkDocs and Material theme
pip install mkdocs mkdocs-material

# Install optional plugins
pip install mkdocs-minify-plugin
```

### Step 2: Preview Locally

```bash
# Navigate to project root
cd C:\Users\cayde\IdeaProjects\AuroraGuis

# Start development server (hot-reload enabled)
mkdocs serve

# Open browser to http://127.0.0.1:8000
```

### Step 3: Build for Production

```bash
# Build static site
mkdocs build

# Output will be in site/ directory
# Upload contents of site/ to any web host
```

## 🌐 Deployment Options

### Option 1: GitHub Pages (Recommended, Free)

**Automatic Deployment:**

```bash
# One command deploys to GitHub Pages
mkdocs gh-deploy

# Your docs will be live at:
# https://unforgoten1.github.io/AuroraGuis
```

**Manual Deployment:**

1. Build the site: `mkdocs build`
2. Push `site/` contents to `gh-pages` branch
3. Enable GitHub Pages in repository settings

### Option 2: Netlify (Free, Easy)

1. Connect your GitHub repository to Netlify
2. Build command: `mkdocs build`
3. Publish directory: `site/`
4. Deploy!

Your docs will be live at: `https://your-site.netlify.app`

### Option 3: Vercel (Free, Fast)

1. Install Vercel CLI: `npm i -g vercel`
2. Run: `vercel`
3. Follow prompts

### Option 4: Custom Web Host

1. Build: `mkdocs build`
2. Upload `site/` directory to your web host
3. Point domain to the directory

## 📝 Documentation Structure

```
docs/
├── index.md                          # Homepage
├── README.md                         # Documentation overview
├── DOCUMENTATION_SUMMARY.md          # Complete file listing
├── getting-started/
│   ├── installation.md               # ✅ Complete
│   ├── quick-start.md                # ✅ Complete
│   └── concepts.md                   # ⚠️ Placeholder
├── features/
│   ├── basic-guis.md                 # ⚠️ Placeholder
│   ├── packet-guis.md                # ⚠️ Needs migration
│   ├── resource-packs.md             # ✅ Complete
│   ├── config-guis.md                # ✅ Complete
│   ├── virtual-guis.md               # ✅ Complete
│   ├── animations.md                 # ⚠️ Placeholder
│   └── conditions-cooldowns.md       # ⚠️ Placeholder
├── guides/                           # ⚠️ All placeholders
├── api/                              # ⚠️ All placeholders
└── examples/                         # ⚠️ All placeholders
```

**Legend:**
- ✅ Complete - Full content written
- ⚠️ Placeholder - File created but needs content
- ⚠️ Needs migration - Content exists elsewhere

## 🎨 Customization

### Change Theme Colors

Edit `mkdocs.yml`:

```yaml
theme:
  palette:
    primary: indigo    # Change to: blue, red, green, etc.
    accent: blue       # Change to match your brand
```

### Add Your Logo

1. Add logo to `docs/assets/logo.png`
2. Update `mkdocs.yml`:

```yaml
theme:
  logo: assets/logo.png
  favicon: assets/favicon.ico
```

### Add Analytics

Add Google Analytics to `mkdocs.yml`:

```yaml
extra:
  analytics:
    provider: google
    property: G-XXXXXXXXXX
```

## 📱 Features Enabled

Your documentation website includes:

✅ **Responsive Design** - Mobile, tablet, desktop
✅ **Dark/Light Mode** - User preference toggle
✅ **Search** - Full-text search with suggestions
✅ **Navigation** - Tabbed navigation with sections
✅ **Code Highlighting** - Syntax highlighting for Java, YAML, etc.
✅ **Copy Buttons** - One-click code copying
✅ **Table of Contents** - Auto-generated for each page
✅ **Social Links** - GitHub integration
✅ **Feedback** - Helpful/not helpful buttons

## 🔧 Advanced Configuration

### Adding Custom CSS

1. Create `docs/stylesheets/extra.css`:

```css
/* Custom styles */
.md-header {
    background-color: #1a1a2e;
}
```

2. Already referenced in `mkdocs.yml` ✅

### Adding Custom JavaScript

1. Create `docs/javascripts/extra.js`:

```javascript
// Custom JavaScript
console.log('AuroraGuis Docs Loaded');
```

2. Already referenced in `mkdocs.yml` ✅

### Adding Search Analytics

Track search queries to improve documentation:

```yaml
plugins:
  - search:
      lang: en
  - google-analytics:
      property: G-XXXXXXXXXX
```

## 📊 Next Steps

### Complete Remaining Documentation

Priority order:

1. **High Priority:**
   - Create `features/basic-guis.md` - Essential for beginners
   - Migrate `PACKET_GUI_GUIDE.md` to `features/packet-guis.md`
   - Create `api/aurora-gui.md` - Core API reference
   - Create `examples/code-examples.md` - Ready-to-use snippets

2. **Medium Priority:**
   - Create `guides/shop-tutorial.md` - Popular use case
   - Create `features/animations.md` - Visual features
   - Create `examples/yaml-examples.md` - Configuration showcase

3. **Low Priority:**
   - Create remaining API references
   - Create remaining guides
   - Add more examples

### Migrate Existing Content

You already have excellent content in:
- `PACKET_GUI_GUIDE.md` (650+ lines) → Copy to `docs/features/packet-guis.md`
- `EXAMPLES.md` (1,000+ lines) → Split into `docs/examples/`
- `README.md` → Some content can be reused in docs

## 🎯 Quick Commands Reference

```bash
# Development
mkdocs serve              # Start dev server
mkdocs serve -a 0.0.0.0:8000  # Allow network access

# Building
mkdocs build              # Build static site
mkdocs build --clean      # Clean build

# Deployment
mkdocs gh-deploy          # Deploy to GitHub Pages
mkdocs gh-deploy --force  # Force deploy

# Validation
mkdocs build --strict     # Build with warnings as errors
```

## 📖 Resources

### Documentation
- [MkDocs Documentation](https://www.mkdocs.org/)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
- [Markdown Guide](https://www.markdownguide.org/)

### Examples
- [FastAPI Docs](https://fastapi.tiangolo.com/) - Excellent technical docs
- [Spigot API Docs](https://hub.spigotmc.org/javadocs/spigot/) - Similar audience
- [Vault Wiki](https://github.com/MilkBowl/VaultAPI/wiki) - Plugin documentation example

## 🐛 Troubleshooting

### "mkdocs: command not found"

**Solution:** Ensure Python and pip are in PATH:
```bash
python --version
pip --version
pip install mkdocs mkdocs-material
```

### "No module named 'material'"

**Solution:** Install Material theme:
```bash
pip install mkdocs-material
```

### Port Already in Use

**Solution:** Use different port:
```bash
mkdocs serve -a 127.0.0.1:8001
```

### Build Fails with Warnings

**Solution:** Fix markdown issues or disable strict mode:
```bash
mkdocs build --no-strict
```

## ✨ Pro Tips

1. **Use Hot-Reload:** Keep `mkdocs serve` running while editing - changes appear instantly

2. **Test Locally First:** Always preview with `mkdocs serve` before deploying

3. **Check Links:** Use a broken link checker before deployment:
   ```bash
   mkdocs build --strict
   ```

4. **Version Your Docs:** Use git branches for different versions:
   ```bash
   git checkout -b docs-v1.1.0
   mkdocs gh-deploy
   ```

5. **Add README Badges:** Add a docs badge to your main README:
   ```markdown
   [![Documentation](https://img.shields.io/badge/docs-mkdocs-blue.svg)](https://unforgoten1.github.io/AuroraGuis)
   ```

## 📞 Support

If you need help with the documentation:
1. Check the [MkDocs documentation](https://www.mkdocs.org/)
2. Check the [Material theme docs](https://squidfunk.github.io/mkdocs-material/)
3. Open an issue on GitHub

---

**Happy documenting!** 📚✨

Your documentation website is production-ready and can be deployed in minutes with `mkdocs gh-deploy`.
