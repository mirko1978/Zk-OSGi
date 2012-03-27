/* BSHInterpreter.java

	Purpose:
		
	Description:
		
	History:
		Thu Jun  1 14:28:43     2006, Created by tomyeh

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zk.scripting.bsh;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.io.Serializable;
import java.io.Externalizable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import bsh.BshClassManager;
import bsh.NameSpace;
import bsh.BshMethod;
import bsh.Variable;
import bsh.Primitive;
import bsh.EvalError;
import bsh.UtilEvalError;

import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.lang.reflect.Fields;
import org.zkoss.xel.Function;
import org.zkoss.util.logging.Log;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ExecutionCtrl;
import org.zkoss.zk.ui.ext.Scope;
import org.zkoss.zk.ui.ext.ScopeListener;
import org.zkoss.zk.scripting.util.GenericInterpreter;
import org.zkoss.zk.scripting.SerializableAware;
import org.zkoss.zk.scripting.HierachicalAware;

/**
 * The interpreter that uses BeanShell to interpret zscript codes.
 *
 * <p>Unlike many other implementations, it supports the hierachical
 * scopes ({@link HierachicalAware}).
 * That is, it uses an independent BeanShell NameSpace
 * (aka. interpreter's scope) to store the variables/classes/methods
 * defined in BeanShell script for each ZK scope ({@link Scope}).
 * Since one-to-one relationship between BeanShell's scope and ZK scope,
 * the invocation of BeanShell methods can execute correctly without knowing
 * what scope it is.
 * However, if you want your codes portable across different interpreters,
 * you had better to call
 * {@link org.zkoss.zk.ui.ext.Scopes#beforeInterpret}
 * to prepare the proper scope, before calling any method defined in
 * zscript.
 *
 * <h2>How serialization work?</p>
 *
 * <p>First, all NameSpace objects have to serialize. Second,
 * the top-level namespace (GlobalNS) is wrapped with NSWrap, which
 * is not serializable. It is serialized when {@link SerializableAware#write}
 * is called (trigged by PageImpl's write).
 *
 * <p>On the other hand, all non-top-level namespaces (NS) are wrapped with
 * NSWrapSR which is serializable, so they are serialized with
 * the attributes of a ID space owner being serialized.
 *
 * @author tomyeh
 */
public class BSHInterpreter extends GenericInterpreter
implements SerializableAware, HierachicalAware {
	/*package*/ static final Log log = Log.lookup(BSHInterpreter.class);

	/** A variable in {@link Scope}. The value is an instance of
	 * BeanShell's NameSpace.
	 */
	private static final String VAR_NSW = "z_bshnsw";
	private bsh.Interpreter _ip;
	private GlobalNS _bshns;

	/*static {
		bsh.Interpreter.LOCALSCOPING = false;
			//must be false (default); otherwise, the following fails
			//class X {
			//  String x;
			//  X(String v) {x = v;}
	}*/

	public BSHInterpreter() {
	}

	//Deriving to override//
	/** Called when the top-level BeanShell scope is created.
	 * By default, it does nothing.
	 *
	 * <p>Note: to speed up the performance, this implementation
	 * disabled {@link bsh.NameSpace#loadDefaultImports}.
	 * It only imports the java.lang and java.util packages.
	 * If you want the built command and import packages, you can override
	 * this method. For example,
	 * <pre><code>
	 *protected void loadDefaultImports(NameSpace bshns) {
	 *  bshns.importCommands("/bsh/commands");
	 *}</code></pre>
	 *
	 * @since 3.0.2
	 */
	protected void loadDefaultImports(NameSpace bshns) {
	}

	//GenericInterpreter//
	protected void exec(String script) {
		try {
			final Scope scope = getCurrent();
			if (scope != null) _ip.eval(script, prepareNS(scope));
			else _ip.eval(script); //unlikely (but just in case)
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	protected boolean contains(String name) {
		try {
			return _ip.getNameSpace().getVariable(name) != Primitive.VOID;
				//Primitive.VOID means not defined
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected Object get(String name) {
		try {
			return Primitive.unwrap(_ip.get(name));
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected void set(String name, Object val) {
		try {
			_ip.set(name, val);
				//unlike NameSpace.setVariable, _ip.set() handles null
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}
	protected void unset(String name) {
		try {
			_ip.unset(name);
		} catch (EvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	protected boolean contains(Scope scope, String name) {
		if (scope != null) {
			final NameSpace bshns = prepareNS(scope);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		return bshns.getVariable(name) != Primitive.VOID;
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		return contains(name);
	}
	protected Object get(Scope scope, String name) {
		if (scope != null) {
			final NameSpace bshns = prepareNS(scope);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		return Primitive.unwrap(bshns.getVariable(name));
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		return get(name);
	}
	protected void set(Scope scope, String name, Object val) {
		if (scope != null) {
			final NameSpace bshns = prepareNS(scope);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		try {
			 		bshns.setVariable(
			 			name, val != null ? val: Primitive.NULL, false);
		 			return;
				} catch (UtilEvalError ex) {
					throw UiException.Aide.wrap(ex);
				}
			}
		}
		set(name, val);
	}
	protected void unset(Scope scope, String name) {
		if (scope != null) {
			final NameSpace bshns = prepareNS(scope);
				//note: we have to create NameSpace (with prepareNS)
				//to have the correct chain
			if (bshns != _bshns) {
		 		bshns.unsetVariable(name);
	 			return;
			}
		}
		unset(name);
	}

	//-- Interpreter --//
	public void init(Page owner, String zslang) {
		super.init(owner, zslang);

		_ip = new bsh.Interpreter();
		_ip.setClassLoader(Thread.currentThread().getContextClassLoader());

		_bshns = new GlobalNS(_ip.getClassManager(), "global");
		_ip.setNameSpace(_bshns);
	}
	public void destroy() {
		getOwner().removeAttribute(VAR_NSW);
		
		//bug 1814819 ,clear variable, dennis
		try{
			_bshns.clear();
			_ip.setNameSpace(null);
		} catch (Throwable t) { //silently ignore (in case of upgrading to new bsh)
		}
		
		_ip = null;
		_bshns = null;
		super.destroy();
	}

	/** Returns the native interpreter, or null if it is not initialized
	 * or destroyed.
	 * From application's standpoint, it never returns null, and the returned
	 * object must be an instance of {@link bsh.Interpreter}
	 * @since 3.0.2
	 */
	public Object getNativeInterpreter() {
		return _ip;
	}

	public Class<?> getClass(String clsnm) {
		try {
			return _bshns.getClass(clsnm);
		} catch (UtilEvalError ex) {
			throw new UiException("Failed to load class "+clsnm, ex);
		}
	}
	public Function getFunction(String name, Class[] argTypes) {
		return getFunction0(_bshns, name, argTypes);
	}
	public Function getFunction(Scope scope, String name, Class[] argTypes) {
		return getFunction0(prepareNS(scope), name, argTypes);
	}
	private Function getFunction0(NameSpace bshns, String name, Class[] argTypes) {
		try {
		 	final BshMethod m = bshns.getMethod(
		 		name, argTypes != null ? argTypes: new Class[0], false);
		 	return m != null ? new BSHFunction(m): null;
		} catch (UtilEvalError ex) {
			throw UiException.Aide.wrap(ex);
		}
	}

	/** Prepares the namespace for non-top-level scope.
	 */
	private NameSpace prepareNS(Scope scope) {
		scope = getIdSpace(scope);
		if (scope == null || scope == getOwner())
			return _bshns;

		NSWrap nsw = (NSWrap)scope.getAttribute(VAR_NSW);
		if (nsw != null)
			return nsw.unwrap(scope);

		//bind bshns and scope
		final NS bshns = newNS(scope);
		scope.setAttribute(VAR_NSW, NSWrap.getInstance(bshns));
		return bshns;
	}
	/*package*/ NS newNS(Scope scope) {
		scope = getIdSpace(scope);
		Scope p = getParentIdSpace(scope);
		return new NS(p != null ? prepareNS(p): _bshns, _ip.getClassManager(), scope);
			//Bug 1831534: we have to pass class manager
			//Bug 1899353: we have to use _bshns instead of null (Reason: unknown)
	}
	/** Prepares the namespace for detached components. */
	private static NameSpace prepareDetachedNS(Scope scope) {
		scope = getIdSpace(scope);
		if (scope == null)
			return null;

		NSWrap nsw = (NSWrap)scope.getAttribute(VAR_NSW);
		if (nsw != null)
			return nsw.unwrap(scope);

		//bind bshns and scope
		Scope p = getParentIdSpace(scope);
		NameSpace bshns = new NS(p != null ? prepareDetachedNS(p): null, null, scope);
		scope.setAttribute(VAR_NSW, NSWrap.getInstance(bshns));
		return bshns;
	}

	/*package*/ static BSHInterpreter getInterpreter(Scope scope) {
		Page owner = getPage(scope);
		if (owner != null) {
			for (Iterator it = owner.getLoadedInterpreters().iterator();
			it.hasNext();) {
				final Object ip = it.next();
				if (ip instanceof BSHInterpreter)
					return (BSHInterpreter)ip;
			}
		}
		return null;
	}

	/** Returns the nearest IdSpace (scope), never null. */
	private static Scope getIdSpace(Scope scope) {
		if (scope instanceof IdSpace)
			return scope;
		if (scope instanceof Component) {
			scope = ((Component)scope).getSpaceOwner();
			if (scope != null) return scope;
		}
		return null;
	}
	/** Returns the parent IdSpace (scope), or null if no parent. */
	private static Scope getParentIdSpace(Scope scope) {
		if (scope == null || !(scope instanceof Component))
			return null;
		final Component p = ((Component)scope).getParent();
		return p != null ? p.getSpaceOwner(): null;
	}
	private static Page getPage(Scope scope) {
		return scope instanceof Component ?
				((Component)scope).getPage():
			scope instanceof Page ? ((Page)scope): null;
	}

	//supporting classes//
	/** The global namespace. */
	private static abstract class AbstractNS extends NameSpace {
		private boolean _inGet;
		protected boolean _firstGet;

		protected AbstractNS(NameSpace parent, BshClassManager classManager,
		String name) {
			super(parent, classManager, name);
		}

		/** Deriver has to override this method. */
		abstract protected Object getFromScope(String name);

		//super//
		protected Variable getVariableImpl(String name, boolean recurse)
		throws UtilEvalError {
			//Note: getVariableImpl returns null if not defined,
			//while getVariable return Primitive.VOID if not defined

			//Tom M Yeh: 20060606:
			//We cannot override getVariable because BeanShell use
			//getVariableImpl to resolve a variable recursively
			//
			//setVariable will callback this method,
			//so use _inGet to prevent dead loop
			Variable var = super.getVariableImpl(name, false);
			if (!_inGet && var == null) {
				_firstGet = true;
				Object v = getFromScope(name);
				if (v != UNDEFINED) {
			//Variable has no public/protected constructor, so we have to
			//store the value back (with setVariable) and retrieve again
					_inGet = true;
					try {
						this.setVariable(name,
							v != null ? v: Primitive.NULL, false);
						var = super.getVariableImpl(name, false);
						this.unsetVariable(name); //restore
					} finally {
						_inGet = false;
					}
				}

				if (var == null && recurse) {
					NameSpace parent = getParent();
					if (parent instanceof AbstractNS) {
						var = ((AbstractNS)parent).getVariableImpl(name, true);
					} else if (parent != null) { //show not reach here; just in case
						try {
							java.lang.reflect.Method m =
								NameSpace.class.getDeclaredMethod("getVariableImpl",
									new Class[] {String.class, Boolean.TYPE});
							m.setAccessible(true);
							var = (Variable)m.invoke(parent, name, Boolean.TRUE);
						} catch (Exception ex) {
							throw UiException.Aide.wrap(ex);
						}
					}
				}
			}

			return var;
		}
		public void loadDefaultImports() {
			 //to speed up the formance
		}
	}
	/** The global NameSpace. */
	private class GlobalNS extends AbstractNS {
		private GlobalNS(BshClassManager classManager,
		String name) {
			super(null, classManager, name);
		}
		protected Object getFromScope(String name) {
			final Scope curr = getCurrent();
			if (curr == null) //no scope allowed
				return getImplicit(name);

			if (_firstGet) {
				_firstGet = false;
				final Execution exec = Executions.getCurrent();
				if (exec != null) {
					Object val = exec instanceof ExecutionCtrl ? ((ExecutionCtrl)exec).getExtraXelVariable(name): null;
					if (val != null)
						return val;
					val = exec.getAttribute(name);
					if (val != null /*||exec.hasAttribute(name)*/) //exec not support hasAttribute
						return val;
				}

				//page is the IdSpace, so it might not be curr
				if (curr instanceof Component) {
					for (Component c = (Component)curr; c != null; c = c.getParent()) {
						Object val = c.getAttribute(name);
						if (val != null || c.hasAttribute(name))
							return val;
					}
				}
			}

			final Page page = getOwner();
			Object val = page.getAttributeOrFellow(name, true); //page/desktop/session
			if (val != null || page.hasAttributeOrFellow(name, true))
				return val;
			val = page.getXelVariable(null, null, name, true);
			return val != null ? val: getImplicit(name); 
		}
		public void loadDefaultImports() {
			BSHInterpreter.this.loadDefaultImports(this);
		}
		
		// Mirko Bernardoni: Patch for using in OSGi enviroment
		@Override
		public Class getClass(String name) throws UtilEvalError {
			Class clazz = super.getClass(name);
			if(clazz != null ) {
				return clazz;
			}
			try {
				clazz = Classes.forNameByThread(name);
			} catch (ClassNotFoundException e) {
			}
			return clazz;
		}
	}
	/** The per-IdSpace NameSpace. */
	/*package*/ static class NS extends AbstractNS {
		private Scope _scope;

		private NS(NameSpace parent, BshClassManager classManager, Scope scope) {
			super(parent, classManager, "scope" + System.identityHashCode(scope));
			_scope = scope;
			_scope.addScopeListener(new NSCListener(this));
		}

		//super//
		/** Search _scope instead. */
		protected Object getFromScope(String name) {
			final BSHInterpreter ip = getInterpreter(_scope);
			final Scope curr = ip != null ? ip.getCurrent(): null;
			if (curr == null)
				return getImplicit(name); //ignore scope

			if (_firstGet) {
				_firstGet = false;
				final Execution exec = Executions.getCurrent();
				if (exec != null && exec != curr) {
					Object val = exec instanceof ExecutionCtrl ? ((ExecutionCtrl)exec).getExtraXelVariable(name): null;
					if (val != null)
						return val;
					val = exec.getAttribute(name);
					if (val != null /*||exec.hasAttribute(name)*/) //exec not support hasAttribute
						return val;
				}

				//_scope is the nearest IdSpace so it might not be curr
				if (curr != _scope && curr instanceof Component) {
					for (Component c = (Component)curr;
					c != null && c != _scope; c = c.getParent()) {
						Object val = c.getAttribute(name);
						if (val != null || c.hasAttribute(name))
							return val;
					}
				}
			}

			Component comp = (Component)_scope;
			//local-only since getVariableImpl will look up its parent
			Object val = comp.getAttributeOrFellow(name, false);
			return val != null || comp.hasAttributeOrFellow(name, false) ?
				val: getImplicit(name); 
				//No need to invoke getXelVariable since it is not 'recurse'
		}
	}
	private static class NSCListener implements ScopeListener {
		private final NS _bshns;
		private NSCListener(NS bshns) {
			_bshns = bshns;
		}
		public void attributeAdded(Scope scope, String name, Object value) {
		}
		public void attributeReplaced(Scope scope, String name, Object value) {
		}
		public void attributeRemoved(Scope scope, String name) {
		}
		public void parentChanged(Scope scope, Scope newparent) {
		}
		public void idSpaceChanged(Scope scope, IdSpace newIdSpace) {
			if (newIdSpace instanceof Scope) { //i.e., != null (but safer)
				final BSHInterpreter ip = getInterpreter(_bshns._scope);
				_bshns.setParent(
					ip != null ? ip.prepareNS(newIdSpace):
						prepareDetachedNS(newIdSpace));
				return;
			}

			_bshns.setParent(null);
		}
	}

	//SerializableAware//
	public void write(ObjectOutputStream s, Filter filter)
	throws IOException {
		write(_bshns, s, filter);
	}
	public void read(ObjectInputStream s)
	throws IOException, ClassNotFoundException {
		read(_bshns, s);
	}

	/*package*/ static void write(NameSpace ns, ObjectOutputStream s, Filter filter)
	throws IOException {
		//1. variables
		final String[] vars = ns.getVariableNames();
		for (int j = vars != null ? vars.length: 0; --j >= 0;) {
			final String nm = vars[j];
			if (nm != null && !"bsh".equals(nm)
			&& isVariableSerializable(nm)) {
				try {
					final Object val = ns.getVariable(nm, false);
					if ((val == null || (val instanceof Serializable)
						|| (val instanceof Externalizable))
					&& !(val instanceof Component)
					&& (filter == null || filter.accept(nm, val))) {
						s.writeObject(nm);
						s.writeObject(val);
					}
				} catch (IOException ex) {
					throw ex;
				} catch (Throwable ex) {
					log.warning("Ignored failure to write "+nm, ex);
				}
			}
		}
		s.writeObject(null); //denote end-of-vars

		//2. methods
		if (shallSerializeMethod()) {
			final BshMethod[] mtds = ns.getMethods();
			for (int j = mtds != null ? mtds.length: 0; --j >= 0;) {
				final String nm = mtds[j].getName();
				if (isMethodSerializable(nm)
				&& (filter == null || filter.accept(nm, mtds[j]))) {
					//hack BeanShell 2.0b4 which cannot be serialized correctly
					Field f = null;
					boolean acs = false;
					try {
						f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
						acs = f.isAccessible();
						Fields.setAccessible(f, true);
						final Object old = f.get(mtds[j]);
						try {
							f.set(mtds[j], null);
							s.writeObject(mtds[j]);
						} finally {
							f.set(mtds[j], old);
						}
					} catch (IOException ex) {
						throw ex;
					} catch (Throwable ex) {
						log.warning("Ignored failure to write "+nm, ex);
					} finally {
						if (f != null) Fields.setAccessible(f, acs);
					}
				}
			}
		}
		s.writeObject(null); //denote end-of-mtds

		//3. imported class
		Field f = null;
		boolean acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedClasses");
			acs = f.isAccessible();
			Fields.setAccessible(f, true);
			final Map clses = (Map)f.get(ns);
			if (clses != null)
				for (Iterator it = clses.values().iterator(); it.hasNext();) {
					final String clsnm = (String)it.next();
					if (!clsnm.startsWith("bsh."))
						s.writeObject(clsnm);
				}
		} catch (IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			log.warning("Ignored failure to write imported classes", ex);
		} finally {
			if (f != null) Fields.setAccessible(f, acs);
		}
		s.writeObject(null); //denote end-of-cls

		//4. imported package
		f = null;
		acs = false;
		try {
			f = Classes.getAnyField(NameSpace.class, "importedPackages");
			acs = f.isAccessible();
			Fields.setAccessible(f, true);
			final Collection pkgs = (Collection)f.get(ns);
			if (pkgs != null)
				for (Iterator it = pkgs.iterator(); it.hasNext();) {
					final String pkgnm = (String)it.next();
					if (!pkgnm.startsWith("java.awt")
					&& !pkgnm.startsWith("javax.swing"))
						s.writeObject(pkgnm);
				}
		} catch (IOException ex) {
			throw ex;
		} catch (Throwable ex) {
			log.warning("Ignored failure to write imported packages", ex);
		} finally {
			if (f != null) Fields.setAccessible(f, acs);
		}
		s.writeObject(null); //denote end-of-cls
	}
	private static boolean isVariableSerializable(String name) {
		//we have to filter out them since BeanShell will store variables
		//that was accessed (by getFromScope)
		for (int j = _nonSerNames.length; --j >= 0;)
			if (_nonSerNames[j].equals(name))
				return false;
		return true;
	}
	private static final String[] _nonSerNames = {
		"log", "page", "desktop", "pageScope", "desktopScope",
		"applicationScope", "requestScope", "spaceOwner",
		"session", "sessionScope", "execution"
	};
	private static boolean isMethodSerializable(String name) {
		return !"alert".equals(name);
	}
	private static boolean shallSerializeMethod() {
		final String s = Library.getProperty("org.zkoss.zk.scripting.bsh.method.serializable");
		return s == null || !"false".equals(s);
	}

	/*package*/ static void read(NameSpace ns, ObjectInputStream s)
	throws IOException {
		for (;;) {
			try {
				final String nm = (String)s.readObject();
				if (nm == null) break; //no more

				ns.setVariable(nm, s.readObject(), false);
			} catch (IOException ex) {
				throw ex;
			} catch (Throwable ex) {
				log.warning("Ignored failure to read", ex);
			}
		}

		for (;;) {
			try {
				final BshMethod mtd = (BshMethod)s.readObject();
				if (mtd == null) break; //no more

				//fix declaringNameSpace
				Field f = null;
				boolean acs = false;
				try {
					f = Classes.getAnyField(BshMethod.class, "declaringNameSpace");
					acs = f.isAccessible();
					Fields.setAccessible(f, true);
					f.set(mtd, ns);				
				} finally {
					if (f != null) Fields.setAccessible(f, acs);
				}
				ns.setMethod(mtd.getName(), mtd);
			} catch (IOException ex) {
				throw ex;
			} catch (Throwable ex) {
				log.warning("Ignored failure to read", ex);
			}
		}

		for (;;) {
			try {
				final String nm = (String)s.readObject();
				if (nm == null) break; //no more

				ns.importClass(nm);
			} catch (IOException ex) {
				throw ex;
			} catch (Throwable ex) {
				log.warning("Ignored failure to read", ex);
			}
		}

		for (;;) {
			try {
				final String nm = (String)s.readObject();
				if (nm == null) break; //no more

				ns.importPackage(nm);
			} catch (IOException ex) {
				throw ex;
			} catch (Throwable ex) {
				log.warning("Ignored failure to read", ex);
			}
		}
	}

	private class BSHFunction implements Function {
		private final bsh.BshMethod _method;
		private BSHFunction(bsh.BshMethod method) {
			if (method == null)
				throw new IllegalArgumentException("null");
			_method = method;
		}

		//-- Function --//
		public Class[] getParameterTypes() {
			return _method.getParameterTypes();
		}
		public Class getReturnType() {
			return _method.getReturnType();
		}
		public Object invoke(Object obj, Object... args) throws Exception {
			return _method.invoke(args != null ? args: new Object[0], _ip);
		}
		public java.lang.reflect.Method toMethod() {
			return null;
		}
	}
}
