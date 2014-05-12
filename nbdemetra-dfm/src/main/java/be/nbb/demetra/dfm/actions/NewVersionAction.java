/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.nbb.demetra.dfm.actions;

import be.nbb.demetra.dfm.DfmDocumentManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import ec.tss.dfm.VersionedDfmDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools",
        id = "be.nbb.demetra.dfm.actions.NewVersionAction")
@ActionRegistration(displayName = "#CTL_NewVersion")
@ActionReferences({
    @ActionReference(path = DfmDocumentManager.ITEMPATH, position = 2720)
})
@Messages("CTL_NewVersion=New version")
public final class NewVersionAction implements ActionListener {

    private final ItemWsNode context;

    public NewVersionAction(ItemWsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WorkspaceItem<?> cur = context.getItem();
        if (cur != null && !cur.isReadOnly()) {
            if (cur.getElement() instanceof VersionedDfmDocument) {
                VersionedDfmDocument doc=(VersionedDfmDocument) cur.getElement();
                IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(cur.getFamily());
                WorkspaceItem<?> ndoc = WorkspaceItem.newItem(cur.getFamily(), mgr.getNextItemName(null), doc.clone());
                context.getWorkspace().add(ndoc);
            }
        }
    }
}
