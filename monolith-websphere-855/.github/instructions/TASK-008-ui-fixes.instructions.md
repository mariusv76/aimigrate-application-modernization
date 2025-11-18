---
applyTo: '**/*'
---

# Migration Task Prompt: UI/UX Fixes and End-to-End Validation

## Mission
You are an AI agent tasked with performing **TASK-008** for the Customer Order Services application: **Fix UI/UX issues and validate complete end-to-end functionality** after Azure migration.

## Task Overview
**Task ID:** TASK-008  
**Task Name:** UI/UX Fixes and End-to-End Validation  
**Priority:** 🔴 CRITICAL  
**Dependencies:** TASK-006 (Azure Integration) completed  
**Estimated Effort:** 80 hours manual / 40 hours AI-assisted  
**Success Criteria:** All UI functionality working, all REST APIs operational, complete end-to-end user flows validated

## Current Issues

### Identified Problems
1. **Product API Failure**: `/jaxrs/Product` endpoint returning 400 Bad Request
2. **UI Not Displaying Data**: Categories and products not rendering in browser
3. **Dojo Toolkit Issues**: Potential module loading or widget rendering problems
4. **JSON Serialization**: Possible issues with complex object graphs

### Working Components
- ✅ Application deployed to Azure Container Apps
- ✅ Database connectivity (PostgreSQL with SSL)
- ✅ Health checks passing
- ✅ Category API returning data (2 categories)
- ✅ Sample data loaded (2 customers, 2 suppliers, 4 categories, 5 products)

## Detailed Instructions

### Phase 1: Investigation & Diagnostics

1. **Analyze Product API Failure**
   - Check Liberty server logs for exceptions
   - Test Product API with different query parameters
   - Verify ProductResource JAX-RS endpoint implementation
   - Check JSON-B serialization for Product entity
   - Test relationships: Product → Category, Product → Supplier
   - **Save findings** to `.vscode/transformation/TASK-008/diagnostics.md`

2. **UI Debugging**
   - **Use Playwright for automated browser testing**
   - Open browser developer console (F12)
   - Check for JavaScript errors in console
   - Verify Dojo module loading (Network tab)
   - Test AJAX calls to REST APIs
   - Check CSS loading issues
   - **Use Playwright to capture console errors and network failures**
   - **Document all errors** in diagnostics file

3. **Create detailed execution plan**
   - List all fixes needed (prioritized)
   - Estimate complexity for each fix
   - Identify testing requirements
   - **Save plan** to `.vscode/transformation/TASK-008/plan.md`

4. **Get user confirmation** before proceeding with fixes

### Phase 2: Branch & Progress Setup

5. **Create a dedicated migration branch**
   - Branch name: `migration/task-008-ui-fixes`
   - Branch from: `agent-test` (current stable branch)
   - Commit message: "chore: create branch for UI/UX fixes"

6. **Initialize progress tracking**
   - Create `.vscode/transformation/TASK-008/progress.md`
   - Initialize with: task start time, issue list, completed fixes
   - Create `.vscode/transformation/TASK-008/todos.md`
   - List all sub-tasks with checkboxes (❌/🔄/✅)

### Phase 3: Backend API Fixes

7. **Fix Product API**
   - **Diagnose root cause** of 400 error
   - **Check query parameters**: Test with/without filters
   - **Fix JSON-B serialization**: 
     - Add `@JsonbTransient` to prevent circular references
     - Use `@JsonbProperty` for field name mapping
     - Handle lazy-loaded collections properly
   - **Test relationships**: Verify Product-Category and Product-Supplier joins
   - **Commit each fix separately** with descriptive messages

8. **Validate All REST Endpoints**
   - Test Category API: `/jaxrs/Category` (already working)
   - Test Product API: `/jaxrs/Product`
   - Test Product by ID: `/jaxrs/Product/{id}`
   - Test Customer API (if applicable)
   - Test Order API (if applicable)
   - **Document all endpoints** with curl examples

9. **Handle Edge Cases**
   - Empty result sets
   - Missing relationships (null categories/suppliers)
   - Large datasets (pagination if needed)
   - Special characters in names/descriptions

### Phase 4: UI Fixes

10. **Fix Dojo Toolkit Integration**
    - Verify CDN loading (1.10.4 compatibility)
    - Check `djconfig` parsing errors
    - Fix module loading timing (use `dojo.ready()`)
    - Verify widget instantiation (TabContainer, DataGrid, etc.)
    - **Use Playwright to test widget rendering and interactions**
    - **Test in browser** after each change

11. **Fix AJAX REST API Calls**
    - Update URLs to use correct base path
    - Handle CORS if needed
    - Add error handling for failed requests
    - Display loading indicators
    - Show user-friendly error messages

12. **Fix Data Rendering**
    - Verify category tree rendering
    - Fix product grid/list display
    - Handle missing images gracefully
    - Format prices correctly
    - Display product details (name, description, price)

13. **Fix CSS/Layout Issues**
    - Verify Claro theme loading
    - Fix responsive layout issues
    - Test on different screen sizes
    - Ensure proper spacing and alignment
    - Fix any visual glitches

### Phase 5: End-to-End Testing

14. **Test Complete User Flows (Use Playwright)**
    - **Use Playwright for automated browser testing of all user flows**
    - **Browse Categories**: Click through category tree
    - **View Products**: Display products in selected category
    - **Product Details**: Click product to see details
    - **Add to Cart**: Test shopping cart functionality (if exists)
    - **Checkout Flow**: Test order placement (if exists)
    - **Search**: Test product search (if exists)
    - **Capture screenshots with Playwright** for documentation
    - **Document results** with screenshots

15. **Cross-Browser Testing (Use Playwright)**
    - **Use Playwright to test across browsers**
    - Test in Chrome (primary)
    - Test in Edge
    - Test in Firefox
    - **Playwright automated validation** for consistency
    - Document browser-specific issues

16. **Performance Testing (Use Playwright)**
    - **Use Playwright for performance measurement**
    - Measure page load time (<3 seconds)
    - Test API response times (<200ms p95)
    - Check for memory leaks (long sessions)
    - **Capture console errors with Playwright**
    - Verify no console errors

### Phase 6: Build & Deploy

17. **Local Validation**
    - Build application: `mvn clean install`
    - Run locally with Liberty: `mvn liberty:dev`
    - Test all UI flows locally
    - Verify no regressions

18. **Docker Build & Test**
    - Build Docker image
    - Run container with Azure configuration
    - Test against Azure PostgreSQL
    - Verify all functionality in container

19. **Deploy to Azure**
    - Build and push to ACR:
      ```powershell
      az acr build --registry customerorderdevacr `
        --image customerorder-api:v1.2.0 `
        --image customerorder-api:latest `
        --file Deployment/Dockerfile.liberty .
      ```
    - Update Container App:
      ```powershell
      az containerapp update `
        --name ca-customerorder-dev `
        --resource-group rg-customerorder-dev `
        --image customerorderdevacr.azurecr.io/customerorder-api:v1.2.0
      ```
    - Wait for deployment to complete
    - **Test deployed application** thoroughly

### Phase 7: Documentation

20. **Update Progress Tracking**
    - Mark all todos complete (✅)
    - Update progress.md with final status
    - Document all fixes applied
    - Include before/after screenshots

21. **Create API Documentation**
    - Document all REST endpoints
    - Provide curl/PowerShell examples
    - Document query parameters
    - Show example responses

22. **Create User Guide**
    - Document UI navigation
    - Explain key features
    - Provide troubleshooting tips
    - Include screenshots of working UI

23. **Generate Summary Report**
    - List all issues fixed
    - Document root causes
    - Provide testing evidence
    - Include lessons learned
    - **Save** to `.vscode/transformation/TASK-008/summary.md`

## Expected Deliverables

1. ✅ Product API fixed and operational
2. ✅ All REST endpoints validated
3. ✅ UI rendering categories correctly
4. ✅ UI rendering products correctly
5. ✅ All user flows working end-to-end
6. ✅ Cross-browser compatibility verified
7. ✅ Performance targets met (<200ms APIs, <3s page load)
8. ✅ Deployed to Azure and validated
9. ✅ Complete API documentation
10. ✅ Complete user guide with screenshots
11. ✅ Summary report with all fixes

## Success Criteria

- [ ] Product API returns 200 OK with valid JSON
- [ ] Category API returns complete category tree
- [ ] UI displays categories in navigation
- [ ] UI displays products in grid/list
- [ ] Product details page shows all information
- [ ] No JavaScript console errors
- [ ] No CSS layout issues
- [ ] Page loads in <3 seconds
- [ ] API responses in <200ms (p95)
- [ ] Works in Chrome, Edge, Firefox
- [ ] All user flows tested and working
- [ ] Deployed to Azure successfully
- [ ] Complete documentation provided

## Testing Checklist

### API Testing
- [ ] GET `/health` returns 200 OK
- [ ] GET `/jaxrs/Category` returns categories
- [ ] GET `/jaxrs/Product` returns products
- [ ] GET `/jaxrs/Product/{id}` returns single product
- [ ] POST endpoints work (if applicable)
- [ ] Error responses are properly formatted

### UI Testing
- [ ] Home page loads without errors
- [ ] Category tree displays correctly
- [ ] Clicking category shows products
- [ ] Product grid/list displays correctly
- [ ] Product images load (or placeholder shown)
- [ ] Product details page works
- [ ] Navigation works (back/forward)
- [ ] No console errors in browser
- [ ] Layout responsive on different sizes
- [ ] **All tests automated with Playwright**

### Integration Testing
- [ ] UI correctly calls REST APIs
- [ ] Data flows from DB → API → UI correctly
- [ ] Error handling works (API errors shown to user)
- [ ] Loading states display properly
- [ ] Empty states handled gracefully

## Progress Reporting

Update `.vscode/transformation/TASK-008/progress.md` with:
- **Activity Log**: Timestamped entries for each significant action
- **Issue Tracking**: Mark issues as diagnosed → fixing → testing → resolved
- **Testing Results**: Document all test outcomes
- **Blockers**: Document any blockers immediately

Update frequency: After completing each phase and sub-task.

## Start Command

Begin by analyzing the Product API failure and UI issues. Create diagnostics.md with findings, then create execution plan for approval.

---

**Ready to begin? Start with Phase 1 investigation.**
