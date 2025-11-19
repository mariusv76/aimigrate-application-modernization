# TASK-008 Execution Plan

**Task:** UI/UX Fixes and End-to-End Validation  
**Created:** November 18, 2025  
**Status:** Ready for Approval  
**Est. Effort:** 40 hours AI-assisted

---

## Executive Summary

Based on diagnostics, **7 issues identified** with **4 critical** fixes required to restore UI functionality. All backend APIs are working correctly; issues are frontend-only (JavaScript, CSS, Dojo widgets).

**Confidence Level:** HIGH - Root causes identified, fixes are straightforward

---

## Critical Findings from Diagnostics

### ✅ What's Working:
- Product API returns correct JSON when provided categoryId
- Category API returns 2 categories with subcategories
- JSON-B serialization working (no circular references)
- Database has correct sample data (5 products, 4 categories)
- Dojo Toolkit CDN loading successfully
- Application deployed and healthy

### ❌ What's Broken:
1. **Category menu not visible** - Empty menu in UI despite API returning data
2. **Categories without subcategories ignored** - "Movies" category not showing
3. **Product grid not visible** - Blank area where products should display
4. **Product API returns 400** - When called without categoryId parameter
5. **Customer API returns 401** - Authentication not implemented
6. **Undefined resource 404** - JavaScript variable used as URL
7. **ProductController timing issue** - loadCatalog may not be executing

---

## Fix Strategy

### Approach: Incremental Fixes with Testing

**Phase 1:** Fix category menu visibility (Critical)  
**Phase 2:** Fix category logic for all categories (Critical)  
**Phase 3:** Fix product grid visibility (Critical)  
**Phase 4:** Add error handling and polish (Medium priority)

**Testing:** After each fix, test locally with Liberty dev mode, then deploy to Azure

---

## Detailed Fixes

### Fix 1: Category Menu Visibility 🔴 CRITICAL

**Problem:** Category menu (`#categoryMenu`) is empty/not visible in UI

**Root Cause Options:**
1. CSS `display:none` hiding the menu
2. Dojo Menu widget not instantiating before loadCatalog() runs
3. loadCatalog() not being called at all

**Investigation Steps:**
1. Check `product.html` for `#categoryMenu` markup
2. Check CSS files for `.categoryMenu` or `#categoryMenu` display properties
3. Add console.log to `loadCatalogSuccess` to verify it runs
4. Check if `dijit.byId("categoryMenu")` returns null

**Fix Implementation:**

**File:** `CustomerOrderServicesWeb/WebContent/dojo_depot/depot/ProductController.js`

**Option A: Add null check and logging**
```javascript
loadCatalogSuccess:function(data,ioArgs)
{
    console.log("[ProductController] loadCatalogSuccess called with data:", data);
    var menu = dijit.byId("categoryMenu");
    console.log("[ProductController] categoryMenu widget:", menu);
    
    if (!menu) {
        console.error("[ProductController] categoryMenu widget not found!");
        return;
    }
    
    // Existing logic...
}
```

**Option B: Wait for widget to be ready**
```javascript
loadCatalog:function()
{
    // Wait for widgets to be parsed
    dojo.addOnLoad(function() {
        var getAccount = {
            url: "jaxrs/Category",
            handleAs: "json",
            load: dojo.hitch(this,this.loadCatalogSuccess),
            error:dojo.hitch(this,this.loadCatalogError)
        };
        dojo.xhrGet(getAccount);
    });
    // Rest of initialization...
}
```

**Option C: Check CSS**
```css
/* Check theme/layout.css */
.categoryMenuPane { display: block !important; }
#categoryMenu { display: block !important; }
```

**Testing:**
1. Add console logging
2. Run locally: `mvn liberty:dev`
3. Open browser dev tools
4. Check console for loadCatalogSuccess log
5. Verify menu widget exists
6. Check if categories are being added to menu

**Success Criteria:**
- Console shows "loadCatalogSuccess called with data: [...]"
- Console shows "categoryMenu widget: [object]"
- Menu items visible in browser (or at least in DOM)

---

### Fix 2: Handle Categories Without Subcategories 🔴 CRITICAL

**Problem:** Categories like "Movies" with `subCategories: null` are skipped

**Current Code:**
```javascript
loadCatalogSuccess:function(data,ioArgs)
{
    var menu = dijit.byId("categoryMenu");
    dojo.forEach(data,dojo.hitch(this, function(item)
    {
        if(item.subCategories)  // <-- PROBLEM: Movies has null subcategories
        {
            // Only processes categories WITH subcategories
        }
    }));
}
```

**Fix Implementation:**

**File:** `CustomerOrderServicesWeb/WebContent/dojo_depot/depot/ProductController.js`

**Lines 48-62 → Replace with:**
```javascript
loadCatalogSuccess:function(data,ioArgs)
{
    console.log("[ProductController] loadCatalogSuccess - Categories:", data);
    var menu = dijit.byId("categoryMenu");
    
    if (!menu) {
        console.error("[ProductController] categoryMenu widget not found!");
        return;
    }
    
    dojo.forEach(data,dojo.hitch(this, function(item)
    {
        if(item.subCategories && item.subCategories.length > 0)
        {
            // Category with subcategories - create popup menu
            var popMenu = new dijit.Menu({parentMenu:menu});
            dojo.addClass(popMenu,"outer");
            dojo.forEach(item.subCategories, dojo.hitch(this,function(subItem)
            {
                var mItem = new dijit.MenuItem({
                    label:subItem.name,
                    title:subItem.categoryID
                });
                dojo.connect(mItem,"onClick",this,this.selectCategory);
                popMenu.addChild(mItem);
            }));
            var pItem = new dijit.PopupMenuItem({label:item.name,popup:popMenu});
            menu.addChild(pItem);
        }
        else
        {
            // Category without subcategories - create regular menu item
            var mItem = new dijit.MenuItem({
                label:item.name,
                title:item.categoryID
            });
            dojo.connect(mItem,"onClick",this,this.selectCategory);
            menu.addChild(mItem);
        }
    }));
    
    console.log("[ProductController] Menu items added:", menu.getChildren().length);
}
```

**Testing:**
1. Run locally with Liberty
2. Open browser dev tools
3. Check console for "Menu items added: X"
4. Inspect DOM for menu items
5. Click on category items
6. Verify selectCategory() is called

**Success Criteria:**
- Console shows "Menu items added: 3" (Electronics + Computers + Phones OR Electronics + Movies)
- "Electronics" shows as popup with "Computers" and "Phones"
- "Movies" shows as regular menu item
- Clicking category triggers selectCategory()

---

### Fix 3: Product Grid Visibility 🔴 CRITICAL

**Problem:** Product grid not visible despite being configured in HTML

**Investigation Steps:**
1. Check if `productStore` (JsonRestStore) is loading data
2. Check if `productGrid` (DataGrid) widget is instantiating
3. Check CSS for `.productGrid` or `#productGrid` visibility
4. Verify formatters (`formatImage`, `formatData`) are working

**Current HTML:**
```html
<div dojoType="dojox.data.JsonRestStore" target="jaxrs/Product" idAttribute="id" jsId="productStore" >
</div>
<table dojoType="dojox.grid.DataGrid" store="productStore" 
query="{categoryId:'2'}" id="productGrid">
```

**Problem Analysis:**
- Store target: `jaxrs/Product` (no base URL)
- Query: `{categoryId:'2'}` (hardcoded to Movies)
- Expected: Should load 1 product (Action Movie Pack)

**Fix Implementation:**

**Option A: Fix JsonRestStore base URL**

**File:** `CustomerOrderServicesWeb/WebContent/product/product.html`

**Lines 22-23 → Replace with:**
```html
<div dojoType="dojox.data.JsonRestStore" 
     target="jaxrs/Product" 
     idAttribute="productId"
     jsId="productStore">
</div>
```

**Note:** Change `idAttribute` from "id" to "productId" to match API response

**Option B: Add console logging to formatters**

**File:** `CustomerOrderServicesWeb/WebContent/dojo_depot/depot/ProductController.js`

**Lines 67-79 → Add logging:**
```javascript
formatImage:function(item)
{
    console.log("[ProductController] formatImage called with:", item);
    return dojo.replace("<img src='{image}' height='100px' width='100px'></img>",{image:item});
},
combineData:function(index,item)
{
    console.log("[ProductController] combineData called with:", item);
    return item;	
},	
formatData:function(item)
{
    console.log("[ProductController] formatData called with:", item);
    return dojo.replace("<div class='productTitle'>{name}</div><div>{price}</div>",item);
},
```

**Option C: Check CSS visibility**

**File:** `CustomerOrderServicesWeb/WebContent/theme/layout.css`

**Add/verify:**
```css
.productMain {
    display: block !important;
    visibility: visible !important;
}

.productGrid {
    display: block !important;
    visibility: visible !important;
    min-height: 200px;
}
```

**Option D: Fix product grid query after category selection**

**File:** `CustomerOrderServicesWeb/WebContent/dojo_depot/depot/ProductController.js`

**Lines 81-86 → Add logging:**
```javascript
selectCategory:function(event)
{
    console.log("[ProductController] selectCategory called with:", event);
    console.log("[ProductController] Category ID:", event.target.parentNode.title);
    
    var grid = dijit.byId("productGrid");
    if (!grid) {
        console.error("[ProductController] productGrid widget not found!");
        return;
    }
    
    console.log("[ProductController] Setting grid query to categoryId:", event.target.parentNode.title);
    grid.setQuery({categoryId:event.target.parentNode.title});
    dojo.place("<div>"+event.target.innerHTML+"</div>","catHeader","only");
}
```

**Testing:**
1. Add console logging to all functions
2. Run locally
3. Check console for:
   - "formatImage called"
   - "formatData called"
   - "selectCategory called"
4. Check browser Network tab for `/jaxrs/Product?categoryId=2` request
5. Inspect DOM for DataGrid elements

**Success Criteria:**
- Product grid visible with 1 product for Movies (ID 2)
- Clicking category loads products for that category
- Product images and names display correctly
- Grid updates when category changes

---

### Fix 4: Product API Validation 🟡 HIGH

**Problem:** Product API returns 400 when categoryId is missing or invalid

**Options:**

**Option A: Keep as-is (Recommended)**
- Business logic: Must select category before viewing products
- UI enforces category selection
- No backend change needed
- Fix: Ensure UI always provides categoryId

**Option B: Add "all products" support**

**File:** `CustomerOrderServicesWeb/src/org/pwte/example/resources/ProductResource.java`

**Lines 95-102 → Modify validation:**
```java
@GET
@Produces(MediaType.APPLICATION_JSON)
public List<Product> getProductsByCategory(@QueryParam(value="categoryId") int categoryId)
{
    System.out.println(categoryId);
    
    // Allow 0 or -1 to mean "all products"
    List<Product> products;
    if(categoryId == 0 || categoryId == -1)
    {
        products = productSearch.loadAllProducts(); // Need to implement this
    }
    else if(categoryId > 0)
    {
        products = productSearch.loadProductsByCategory(categoryId);
    }
    else
    {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    // Existing circular reference handling...
    return products;
}
```

**Recommendation:** Keep as-is (Option A). The current behavior makes business sense and is easy to implement in the UI.

---

### Fix 5: Customer API 401 Error Handling 🟡 MEDIUM

**Problem:** Customer API returns 401, causes JavaScript error in AccountController

**Options:**

**Option A: Disable Account tab** (Quick fix)
```javascript
dojo.ready(function() {
    // Disable Account tab if not authenticated
    var accountTab = dijit.byId("account");
    if (accountTab) {
        accountTab.set("disabled", true);
    }
});
```

**Option B: Handle 401 gracefully**

**File:** `CustomerOrderServicesWeb/WebContent/dojo_depot/depot/AccountController.js`

**Add to error handler:**
```javascript
loadAccountError:function(error)
{
    if (error.status === 401) {
        console.info("[AccountController] Not authenticated - account features disabled");
        // Display "Please log in" message instead of error
        return;
    }
    console.error("[AccountController] Error Loading Account", error);
}
```

**Option C: Implement authentication** (Future task)
- Add login form
- Get JWT token from Entra ID
- Pass token in Authorization header
- Not in scope for TASK-008

**Recommendation:** Option B (graceful error handling)

---

### Fix 6: Undefined Resource 404 🟢 LOW

**Problem:** JavaScript tries to load undefined URL

**Fix:** Add null checks before making requests

**Search for pattern:**
```javascript
dojo.xhrGet({url: someVariable})
```

**Replace with:**
```javascript
if (someVariable) {
    dojo.xhrGet({url: someVariable});
} else {
    console.warn("URL is undefined, skipping request");
}
```

**Implementation:** Review all XHR calls in AccountController and ProductController

---

### Fix 7: CSS Layout Issues 🟢 LOW

**Problem:** Product grid and category menu may be hidden by CSS

**Investigation:**

**File:** `CustomerOrderServicesWeb/WebContent/theme/layout.css`

**Check for:**
```css
.categoryMenuPane { display: ???; }
#categoryMenu { display: ???; }
.productMain { display: ???; }
.productGrid { display: ???; }
```

**Fix if needed:**
```css
.categoryMenuPane {
    display: block;
    width: 200px;
    float: left;
}

.productMain {
    display: block;
    margin-left: 220px;
}

.productGrid {
    display: block;
    width: 100%;
}

#categoryMenu {
    display: block;
}
```

**Testing:** Inspect elements in browser dev tools, check computed styles

---

## Implementation Order

### Phase 1: Critical Fixes (Required for basic functionality)

1. ✅ **Add console logging to ProductController** (30 min)
   - Add logs to loadCatalog, loadCatalogSuccess, selectCategory
   - Add logs to formatImage, formatData
   - Deploy and test to identify which functions are running

2. ✅ **Fix category menu logic** (45 min)
   - Update loadCatalogSuccess to handle categories without subcategories
   - Add null checks for menu widget
   - Test locally with Liberty dev mode

3. ✅ **Fix product grid idAttribute** (15 min)
   - Change from "id" to "productId" in HTML
   - Test grid data loading

4. ✅ **Check and fix CSS visibility** (30 min)
   - Review layout.css for display:none
   - Add explicit display:block if needed
   - Test in browser

### Phase 2: Error Handling (Improve user experience)

5. ✅ **Handle Customer API 401 gracefully** (30 min)
   - Add 401 check to AccountController error handler
   - Display user-friendly message
   - Test Account tab behavior

6. ✅ **Add null checks for undefined resources** (30 min)
   - Review all XHR calls
   - Add validation before making requests
   - Test with browser console

### Phase 3: Testing & Validation

7. ✅ **Playwright automated testing** (2 hours)
   - Test category menu display
   - Test category selection
   - Test product grid display
   - Test product details
   - Capture screenshots for documentation
   - Cross-browser testing (Chrome, Edge, Firefox)

8. ✅ **Performance testing** (1 hour)
   - Measure page load time (<3 seconds)
   - Measure API response times (<200ms p95)
   - Check for memory leaks

### Phase 4: Deployment

9. ✅ **Local validation** (30 min)
   - Build: `mvn clean install`
   - Test: `mvn liberty:dev`
   - Verify all fixes working

10. ✅ **Docker build and test** (30 min)
    - Build image with fixes
    - Test container locally
    - Verify database connectivity

11. ✅ **Deploy to Azure** (30 min)
    - Build and push to ACR as v1.2.0
    - Update Container App
    - Smoke test deployed application
    - Full end-to-end testing

### Phase 5: Documentation

12. ✅ **Update progress tracking** (30 min)
    - Document all fixes applied
    - Include before/after screenshots
    - Update todos as complete

13. ✅ **Create API documentation** (1 hour)
    - Document all REST endpoints
    - Provide PowerShell/curl examples
    - Document query parameters

14. ✅ **Generate summary report** (1 hour)
    - List all issues fixed
    - Document root causes
    - Include testing evidence
    - Lessons learned

---

## Code Changes Summary

### Files to Modify:

1. **CustomerOrderServicesWeb/WebContent/dojo_depot/depot/ProductController.js**
   - Lines 33-62: Add logging and fix loadCatalogSuccess logic
   - Lines 67-79: Add logging to formatters
   - Lines 81-86: Add logging to selectCategory
   - **Est. lines changed:** ~50 lines

2. **CustomerOrderServicesWeb/WebContent/product/product.html**
   - Line 22: Change idAttribute from "id" to "productId"
   - **Est. lines changed:** 1 line

3. **CustomerOrderServicesWeb/WebContent/theme/layout.css**
   - Add/verify display properties for category menu and product grid
   - **Est. lines changed:** ~10-20 lines

4. **CustomerOrderServicesWeb/WebContent/dojo_depot/depot/AccountController.js**
   - Add 401 error handling
   - **Est. lines changed:** ~10 lines

5. **Optional: CustomerOrderServicesWeb/src/org/pwte/example/resources/ProductResource.java**
   - Only if we decide to support "all products"
   - **Est. lines changed:** 0 (keep as-is recommended)

### Total Code Changes:
- **Files modified:** 4 files (HTML, CSS, 2 JavaScript)
- **Lines changed:** ~70 lines
- **Risk level:** LOW (frontend-only changes, no database or backend logic changes)

---

## Testing Strategy

### Unit Testing (Manual):
1. Test Category API: `GET /jaxrs/Category` → 200 OK
2. Test Product API with categoryId: `GET /jaxrs/Product?categoryId=1` → 200 OK
3. Test Product API without categoryId: `GET /jaxrs/Product` → 400 Bad Request (expected)

### Integration Testing (Playwright):
1. Navigate to application URL
2. Wait for page load
3. Verify category menu visible
4. Verify menu contains: "Electronics", "Computers", "Phones", "Movies"
5. Click "Electronics" → Should show submenu
6. Click "Computers" → Should load products in grid
7. Verify product grid shows products
8. Verify product names and prices display
9. Click product → Should show details dialog
10. Test across browsers (Chrome, Edge, Firefox)

### Performance Testing:
1. Measure page load time (target: <3 seconds)
2. Measure Category API response (target: <200ms p95)
3. Measure Product API response (target: <200ms p95)
4. Check for console errors
5. Check for memory leaks (long session)

### Acceptance Criteria:
- [ ] Category menu visible and populated
- [ ] All categories display (including Movies)
- [ ] Clicking category loads products
- [ ] Product grid displays products with images and prices
- [ ] Product details dialog works
- [ ] No critical JavaScript errors
- [ ] Page loads in <3 seconds
- [ ] APIs respond in <200ms (p95)
- [ ] Works in Chrome, Edge, Firefox

---

## Risk Assessment

### Risks:

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Dojo widget lifecycle issues | Medium | High | Add extensive logging, test incrementally |
| CSS conflicts breaking layout | Low | Medium | Use !important carefully, test responsive design |
| Product grid formatters broken | Low | High | Add null checks, test with sample data |
| Azure deployment issues | Low | Medium | Test Docker locally first, use staged rollout |
| Browser compatibility issues | Low | Low | Test with Playwright on multiple browsers |

### Rollback Plan:
1. Keep current v1.1.1 image in ACR
2. If v1.2.0 has issues, revert Container App to v1.1.1
3. Rollback command:
   ```powershell
   az containerapp update `
     --name ca-customerorder-dev `
     --resource-group rg-customerorder-dev `
     --image customerorderdevacr.azurecr.io/customerorder-api:v1.1.1
   ```

---

## Success Metrics

### Before Fixes:
- ❌ Category menu: Empty
- ❌ Product grid: Not visible
- ❌ User can browse products: NO
- ⚠️ JavaScript errors: 3 errors
- ⚠️ API success rate: 50% (Category works, Product fails without categoryId)

### After Fixes (Target):
- ✅ Category menu: Visible with 3-4 items
- ✅ Product grid: Visible with products
- ✅ User can browse products: YES
- ✅ JavaScript errors: 0 critical errors (only 401 for auth, handled gracefully)
- ✅ API success rate: 100% (when used correctly)
- ✅ Page load time: <3 seconds
- ✅ API response time: <200ms p95
- ✅ Cross-browser compatibility: Chrome, Edge, Firefox

---

## Timeline Estimate

### Development: 6-8 hours
- Critical fixes: 2 hours
- Error handling: 1 hour
- Testing setup: 1 hour
- Code review and refinement: 2 hours

### Testing: 3-4 hours
- Playwright test development: 2 hours
- Manual testing: 1 hour
- Cross-browser testing: 1 hour

### Deployment: 1-2 hours
- Docker build and test: 1 hour
- Azure deployment and validation: 1 hour

### Documentation: 2-3 hours
- Progress tracking: 1 hour
- API documentation: 1 hour
- Summary report: 1 hour

### Total: 12-17 hours
**Compressed timeline with AI assistance: 6-8 hours**

---

## Approval Required

### Questions for User:

1. **Product API behavior:**
   - Keep current behavior (requires categoryId)?
   - Or add support for "all products" (categoryId=0)?
   - **Recommendation:** Keep as-is

2. **Customer API authentication:**
   - Handle 401 gracefully and disable Account features?
   - Or implement authentication flow?
   - **Recommendation:** Handle gracefully (auth is future task)

3. **Deployment timing:**
   - Deploy fixes incrementally (after each critical fix)?
   - Or deploy all fixes together at the end?
   - **Recommendation:** Test locally, deploy all fixes together

4. **Testing scope:**
   - Playwright automated testing for all flows?
   - Or manual testing only?
   - **Recommendation:** Playwright for regression testing

---

## Next Steps

1. ✅ Get user approval on this plan
2. ⏭️ Create migration/task-008-ui-fixes branch
3. ⏭️ Initialize progress.md and todos.md
4. ⏭️ Implement critical fixes (Phase 1)
5. ⏭️ Test locally with Liberty dev mode
6. ⏭️ Implement error handling (Phase 2)
7. ⏭️ Playwright testing (Phase 3)
8. ⏭️ Deploy to Azure (Phase 4)
9. ⏭️ Documentation (Phase 5)

---

**Plan Status:** Ready for Review  
**Created:** November 18, 2025  
**Author:** AI Migration Agent  
**Approval Status:** ⏳ Pending User Approval
