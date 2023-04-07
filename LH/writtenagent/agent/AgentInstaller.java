public class AgentInstaller {
  
	/** The created agent jar file name */
	protected static final AtomicReference<String> agentJar = new AtomicReference<String>(null); 
	
	/**
	 * Self installs the agent, then runs a person sayHello in a loop
	 * @param args None
	 */
	public static void main(String[] args) {
		try {
			// Get this JVM's PID
			String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
			// Attach (to ourselves)
		    VirtualMachine vm = VirtualMachine.attach(pid);
		    // Create an agent jar (since we're in DEV mode)
	    	String fileName = createAgent();
	    	// Load the agent into this JVM
	    	vm.loadAgent(fileName);
	    	System.out.println("Agent Loaded");
			ObjectName on = new ObjectName("transformer:service=DemoTransformer");
			System.out.println("Instrumentation Deployed:" + ManagementFactory.getPlatformMBeanServer().isRegistered(on));
	    	// Run sayHello in a loop
			Person person = new Person();
			for(int i = 0; i < 1000; i++) {
				person.sayHello(i);
				person.sayHello("" + (i*-1));
				Thread.currentThread().join(5000);
			}
		} catch (Exception ex) {
			System.err.println("Agent Installation Failed. Stack trace follows...");
			ex.printStackTrace(System.err);
		}
	}
	
	
	/**
	 * Creates the temporary agent jar file if it has not been created
	 * @return The created agent file name
	 */
	public static String createAgent() {
		if(agentJar.get()==null) {
			synchronized(agentJar) {
				if(agentJar.get()==null) {
					FileOutputStream fos = null;
					JarOutputStream jos = null;
					try {
						File tmpFile = File.createTempFile(AgentMain.class.getName(), ".jar");
						System.out.println("Temp File:" + tmpFile.getAbsolutePath());
						tmpFile.deleteOnExit();		
						StringBuilder manifest = new StringBuilder();
						manifest.append("Manifest-Version: 1.0\nAgent-Class: " + AgentMain.class.getName() + "\n");
						manifest.append("Can-Redefine-Classes: true\n");
						manifest.append("Can-Retransform-Classes: true\n");
						manifest.append("Premain-Class: " + AgentMain.class.getName() + "\n");
						ByteArrayInputStream bais = new ByteArrayInputStream(manifest.toString().getBytes());
						Manifest mf = new Manifest(bais);
						fos = new FileOutputStream(tmpFile, false);
						jos = new JarOutputStream(fos, mf);
						addClassesToJar(jos, AgentMain.class, DemoTransformer.class, ModifyMethodTest.class, TransformerService.class, TransformerServiceMBean.class);
						jos.flush();
						jos.close();
						fos.flush();
						fos.close();
						agentJar.set(tmpFile.getAbsolutePath());
					} catch (Exception e) {
						throw new RuntimeException("Failed to write Agent installer Jar", e);
					} finally {
						if(fos!=null) try { fos.close(); } catch (Exception e) {}
					}

				}
			}
		}
		return agentJar.get();
	}
	
	/**
	 * Writes the passed classes to the passed JarOutputStream
	 * @param jos the JarOutputStream
	 * @param clazzes The classes to write
	 * @throws IOException on an IOException
	 */
	protected static void addClassesToJar(JarOutputStream jos, Class<?>...clazzes) throws IOException {
		for(Class<?> clazz: clazzes) {
			jos.putNextEntry(new ZipEntry(clazz.getName().replace('.', '/') + ".class"));
			jos.write(getClassBytes(clazz));
			jos.flush();
			jos.closeEntry();
		}
	}
	
	/**
	 * Returns the bytecode bytes for the passed class
	 * @param clazz The class to get the bytecode for
	 * @return a byte array of bytecode for the passed class
	 */
	public static byte[] getClassBytes(Class<?> clazz) {
		InputStream is = null;
		try {
			is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class");
			ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
			byte[] buffer = new byte[8092];
			int bytesRead = -1;
			while((bytesRead = is.read(buffer))!=-1) {
				baos.write(buffer, 0, bytesRead);
			}
			baos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to read class bytes for [" + clazz.getName() + "]", e);
		} finally {
			if(is!=null) { try { is.close(); } catch (Exception e) {} }
		}
	}	

}
