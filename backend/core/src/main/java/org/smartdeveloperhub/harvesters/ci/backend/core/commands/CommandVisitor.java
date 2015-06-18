package org.smartdeveloperhub.harvesters.ci.backend.core.commands;

public interface CommandVisitor {

	void visitRegisterServiceCommand(RegisterServiceCommand command);

	void visitCreateBuildCommand(CreateBuildCommand command);

	void visitUpdateBuildCommand(UpdateBuildCommand command);

	void visitDeleteBuildCommand(DeleteBuildCommand command);

	void visitCreateExecutionCommand(CreateExecutionCommand command);

	void visitFinishExecutionCommand(FinishExecutionCommand command);

	void visitDeleteExecutionCommand(DeleteExecutionCommand command);

}
