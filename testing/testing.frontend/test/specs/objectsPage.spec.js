let objectsPage = require('../pages/Objects.page')
let {Explorer, SubNav, AddObjectWizard, ObjectEditorPanel} = objectsPage

describe('Peregrine objects page', function () {
    it('should login', function() {
    	objectsPage.open()
        expect( browser.getUrl() ).to.contain('content/admin/objects.html')
    })

    describe('Objects Explorer', function() {
        let exampleFolder
        
        it('should have a folder titled "example"', function(){
        	Explorer.container.waitForVisible()
            const folders = Explorer.folders
            const i = folders.findIndex( folder => folder.text.indexOf('example') > -1 ) 
            exampleFolder = folders[i]
            expect( exampleFolder.text ).to.contain('example')
        })
        
        it('clicking example folder item should load all objects', function(){
        	exampleFolder.linkButton.click()
        	expect( browser.getUrl() ).to.contain('content/objects/example' )
        }) 
        
        let exampleObject
        
        it('should have an object titled "sample"', function(){
        	Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('sample') > -1 ) 
            exampleObject = objects[i]
            expect( exampleObject.text ).to.contain('sample')
        })

    })
    
    describe('Add a new object', function() {
    	it('should see the subnav', function() {
        	SubNav.container.waitForVisible()
        	expect( SubNav.container.isVisible() ).to.equal(true)
        })

        it('clicking add object should navigate to add object wizard', function() {
            SubNav.addObjectButton.click()
            AddObjectWizard.container.waitForVisible()
            expect( browser.getUrl() ).to.contain('content/admin/objects/create.html/path:/content/objects')
        })
        
        let exampleTemplate
        
        it('should see sample in template list', function() {
            AddObjectWizard.container.waitForVisible()
            const templates = AddObjectWizard.templates
            const i = templates.findIndex( template => template.text.indexOf('sample') > -1 ) 
            exampleTemplate = templates[i]
            expect( exampleTemplate.text ).to.contain('sample')
        })
        
        it('selecting sample template', function() {
        	exampleTemplate.linkButton.click()
            expect( exampleTemplate.classAttribute ).to.contain('grey lighten-2')
        })
        
        it('clicking next button should go wizard step 2', function() {
            AddObjectWizard.nextButton.click()
            AddObjectWizard.objectNameField.waitForVisible()
            expect( AddObjectWizard.objectNameField.isVisible() ).to.equal(true)
        })
        
        it('clicking next button should go wizard step 3', function() {
            AddObjectWizard.objectNameField.setValue('myobject1')
            AddObjectWizard.nextButton.click()
            expect( AddObjectWizard.finishButton.isVisible() ).to.equal(true)
        })
        
       let newObject
        
       it('clicking finish button to add this object', function() {
        	AddObjectWizard.finishButton.scroll(0,10000)
            AddObjectWizard.finishButton.click()
            Explorer.container.waitForVisible()
            const objects = Explorer.objects
            const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 ) 
            newObject = objects[i]
            expect( newObject.text ).to.contain('myobject1')
        })

    })
    
    describe('Edit an object', function() {
    	let exampleObject
    	
    	it('find object to edit', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('clicking edit item should load the object editor panel', function(){
    		exampleObject.editButton.click()
    		ObjectEditorPanel.container.waitForVisible()
            expect( ObjectEditorPanel.container.isVisible() ).to.equal(true)
        })
        
        let inputs
        let textEditor
        
        it('editing text field', function(){
    		inputs = ObjectEditorPanel.inputs
    		inputs[0].setValue('first text')
    		expect( inputs[0].getValue() ).to.equal('first text')
    	})
    	
    	it('editing textarea', function(){
    		inputs[1].setValue('first textarea')
    		expect( inputs[1].getValue() ).to.equal('first textarea')
    	})
        
        it('editing text editor', function(){
        	textEditor = ObjectEditorPanel.textEditor
        	textEditor.setValue('first text editor')
        	expect( textEditor.getText() ).to.equal('first text editor')
    	})
        
        it('editing number field', function(){
    		inputs[3].setValue('125')
    		expect( inputs[3].getValue() ).to.equal('125')
    	})
    	
    	it('editing url field', function(){
    		inputs[4].setValue('http://www.peregrine-cms.com')
    		expect( inputs[4].getValue() ).to.equal('http://www.peregrine-cms.com')
    	})
    	
    	it('editing tel field', function(){
    		inputs[5].setValue('9496009999')
    		expect( inputs[5].getValue() ).to.equal('9496009999')
    		ObjectEditorPanel.save.click()
    	})
    	
    	    	
    	let previewContainer
    	
    	it('text field saved', function(){
    		previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(1) > .field-wrap > .wrapper > p').getText() ).to.equal('first text')
    	})
    	
    	it('textarea field saved', function(){
    		//previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(2) > .field-wrap > .wrapper > p').getText() ).to.equal('first textarea')
    	})
    	
    	/*it('text editor saved', function(){
    		//previewContainer = ObjectEditorPanel.previewContainer
    		expect( previewContainer.$('.form-group:nth-child(3) > .field-wrap > .wrapper > p').getText() ).to.equal('first text editor')
    	})*/
    	
    	it('number field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(4) > .field-wrap > .wrapper > p').getText() ).to.equal('125')
    	})
    	
    	it('url field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(5) > .field-wrap > .wrapper > p').getText() ).to.equal('http://www.peregrine-cms.com')
    	})
    	
    	it('tel field saved', function(){
    		expect( previewContainer.$('.form-group:nth-child(6) > .field-wrap > .wrapper > p').getText() ).to.equal('9496009999')
    	})
    })
    
    describe('Delete an object', function() {
    	
    	let exampleObject
    	
    	it('find object to be deleted', function() {
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.not.equal(-1)
	        exampleObject = objects[i]
	    })
    	
    	it('delete object', function() {
    		exampleObject.deleteButton.click()
    		browser.alertAccept();
	    	Explorer.container.waitForVisible()
	        const objects = Explorer.objects
	        const i = objects.findIndex( object => object.text.indexOf('myobject1') > -1 )
	        expect( i ).to.equal(-1)
	    })
    })

})